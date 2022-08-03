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

    fun fetch(sub: Sub): RedditListingThing {
        val token = getToken()
        val listing = Request.Builder()
            .url("https://oauth.reddit.com/r/${sub.sub}/${sub.listing}")
            .header("Authorization", "Bearer ${token.access_token}")
            .build()
        return client.newCall(listing).execute().use { response ->
            val body = response.body?.string().orEmpty()
            gson.fromJson(body, RedditListingThing::class.java)
        }
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
