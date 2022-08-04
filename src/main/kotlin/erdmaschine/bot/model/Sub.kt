package erdmaschine.bot.model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class Sub(
    val guildId: String,
    val channelId: String,
    val sub: String,
    val listing: String,
    val nsfw: Boolean,
) {
    val link = "/r/$sub/$listing"
    val channelKey = "$guildId/$channelId"

    companion object {

        @JvmStatic
        fun fromResultRow(resultRow: ResultRow) =
            Sub(
                resultRow[Subs.guildId],
                resultRow[Subs.channelId],
                resultRow[Subs.sub],
                resultRow[Subs.listing],
                resultRow[Subs.nsfw],
            )
    }

    override fun toString(): String {
        return "Sub[$sub/$listing](G:$guildId,C:$channelId)"
    }
}

object Subs : Table() {
    val guildId = varchar("guildId", 100)
    val channelId = varchar("channelId", 100)
    val sub = varchar("sub", 100)
    val listing = varchar("listing", 100)
    val nsfw = bool("nsfw")

    override val primaryKey = PrimaryKey(guildId, channelId, sub)
}
