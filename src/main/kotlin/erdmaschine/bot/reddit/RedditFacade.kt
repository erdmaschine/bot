package erdmaschine.bot.reddit

import com.google.gson.Gson
import erdmaschine.bot.Env
import erdmaschine.bot.model.Token
import okhttp3.*
import org.slf4j.LoggerFactory
import java.util.*

class RedditFacade(env: Env) {

    private val log = LoggerFactory.getLogger(this::class.java)!!

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
            Gson().fromJson(body, Token::class.java)
                .let {
                    token = it
                    it
                }
        }
    }

    fun fetchListing() {
        val token = getToken()
        val listing = Request.Builder()
            .url("https://oauth.reddit.com/r/de/rising")
            .header("Authorization", "Bearer ${token.access_token}")
            .build()
        client.newCall(listing).execute().use { response ->
            val body = response.body?.string().orEmpty()
            println(body)
        }
    }

}

fun main() {
    val redditFacade = RedditFacade(Env())
    val result = redditFacade.fetchListing()
    println(result)
}
