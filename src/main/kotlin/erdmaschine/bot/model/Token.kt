package erdmaschine.bot.model

import java.time.Instant

data class Token(
    val access_token: String,
    private val expires_in: Long,
) {
    private val validUntil = Instant.now().plusSeconds(expires_in)

    fun isValid() = Instant.now() < validUntil
}
