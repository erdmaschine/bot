package erdmaschine.bot.model

import java.time.Instant

data class RedditTokenResponse(
    val access_token: String,
    val expires_in: Long,
)

class RedditToken(
    val tokenValue: String,
    private val expiresAt: Instant,
) {
    fun isValid(currentTime: Instant) = currentTime < expiresAt
}
