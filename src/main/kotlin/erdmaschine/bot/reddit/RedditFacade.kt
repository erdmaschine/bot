package erdmaschine.bot.reddit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import erdmaschine.bot.Env
import erdmaschine.bot.model.RedditListingThing
import erdmaschine.bot.model.RedditToken
import erdmaschine.bot.model.RedditTokenResponse
import erdmaschine.bot.model.Sub
import okhttp3.*
import org.slf4j.LoggerFactory
import java.time.Clock
import java.util.*

class RedditFacade(env: Env, private val clock: Clock) {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    private val gson: Gson = GsonBuilder().create()

    private val userAgent = "java:erdmaschine:1.0.0"
    private val deviceId = UUID.randomUUID().toString()
    private val credentials = Credentials.basic(env.redditClientId, env.redditClientSecret)

    private var token: RedditToken? = null

    private val client = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
            chain.proceed(newRequest)
        })
        .followRedirects(false)
        .build()

    fun fetch(subs: Collection<Sub>): Map<String, RedditListingThing> {
        val result = mutableMapOf<String, RedditListingThing>()

        subs.forEach { sub ->
            if (result.containsKey(sub.link)) {
                return@forEach
            }

            try {
                val token = getToken()
                val listing = Request.Builder()
                    .url("https://oauth.reddit.com${sub.link}")
                    .header("Authorization", "Bearer ${token.tokenValue}")
                    .build()

                client.newCall(listing).execute().use { response ->
                    val body = response.body?.string().orEmpty()

                    if (!response.isSuccessful) {
                        val message = when {
                            body.startsWith("<!doctype html>", ignoreCase = true) -> "<clipped html body>"
                            else -> body
                        }
                        throw Exception("Error ${response.code} fetching listing [${sub.link}]: $message")
                    }

                    log.info("Fetched [${sub.link}]")
                    result[sub.link] = gson.fromJson(body, RedditListingThing::class.java)
                }
            } catch (exc: Exception) {
                log.error(exc.message, exc)
            }
        }

        return result
    }

    private fun getToken(): RedditToken {
        token?.let {
            if (it.isValid(clock.instant())) {
                return it
            }
            log.info("Reddit token expired, refetching")
        }

        val formBody = FormBody.Builder()
            .add("grant_type", "https://oauth.reddit.com/grants/installed_client")
            .add("device_id", deviceId)
            .add("scope", "read")
            .build()

        val request = Request.Builder()
            .url("https://www.reddit.com/api/v1/access_token")
            .header("Authorization", credentials)
            .post(formBody)
            .build()

        return client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw Exception("Error ${response.code} getting reddit token: $body")
            }

            val tokenResponse = gson.fromJson(body, RedditTokenResponse::class.java)
            RedditToken(tokenResponse.access_token, clock.instant().plusSeconds(tokenResponse.expires_in))
                .also { token = it }
        }
    }
}
