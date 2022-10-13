package erdmaschine.bot.reddit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import erdmaschine.bot.Env
import erdmaschine.bot.model.RedditListingThing
import erdmaschine.bot.model.Sub
import erdmaschine.bot.model.Token
import okhttp3.*
import org.slf4j.LoggerFactory
import java.util.*

class RedditFacade(env: Env) {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    private val gson: Gson = GsonBuilder().create()

    private val userAgent = "java:erdmaschine:1.0.0"
    private val deviceId = UUID.randomUUID().toString()
    private val credentials = Credentials.basic(env.redditClientId, env.redditClientSecret)

    private var token: Token? = null

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

            val token = getToken()
            val listing = Request.Builder()
                .url("https://oauth.reddit.com${sub.link}")
                .header("Authorization", "Bearer ${token.access_token}")
                .build()

            client.newCall(listing).execute().use { response ->
                val body = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    var errorString = "HTML response body"
                    if (!body.contains("<html>")) {
                        errorString = body
                    }
                    throw Exception("Error ${response.code} fetching listing [${sub.link}]: $errorString")
                }

                log.info("Fetched [${sub.link}]")
                result[sub.link] = gson.fromJson(body, RedditListingThing::class.java)
            }
        }

        return result
    }

    private fun getToken(): Token {
        token?.let {
            if (it.isValid()) {
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
                throw Exception("Error getting reddit token: $body")
            }

            gson.fromJson(body, Token::class.java).also { token = it }
        }
    }
}
