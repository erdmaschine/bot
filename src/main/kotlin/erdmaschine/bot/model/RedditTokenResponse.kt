package erdmaschine.bot.model

import java.time.Clock
import java.time.Instant

data class RedditTokenResponse(
    val access_token: String,
    val expires_in: Long,
)

class RedditToken(
    response: RedditTokenResponse,
    clock: Clock
) {
    val accessTokenString: String = response.access_token
    private val validUntil: Instant = clock.instant().plusSeconds(response.expires_in)

    fun isValid(clock: Clock) = clock.instant() < validUntil
}
