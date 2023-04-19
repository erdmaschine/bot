package erdmaschine.bot.model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class EmojiUsage(
    val guildId: String,
    val emojiName: String,
    val used: Int
) {
    companion object {

        @JvmStatic
        fun fromResultRow(resultRow: ResultRow) =
            EmojiUsage(
                resultRow[EmojiUsages.guildId],
                resultRow[EmojiUsages.emojiName],
                resultRow[EmojiUsages.used]
            )
    }

    override fun toString(): String {
        return "EmojiUsage(G:$guildId,E:$emojiName,U:$used)"
    }
}

object EmojiUsages : Table() {
    val guildId = varchar("guildId", 100)
    val emojiName = varchar("emojiId", 100)
    val used = integer("used")

    override val primaryKey = PrimaryKey(guildId, emojiName)
}
