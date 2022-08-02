package erdmaschine.bot.model

import java.util.*

data class Token(
    val access_token: String,
    val token_type: String,
    val device_id: String,
    val expires_in: Long,
    val scope: String,
) {
    fun isValid() = Date().toInstant() < Date().toInstant().plusSeconds(expires_in)
}
