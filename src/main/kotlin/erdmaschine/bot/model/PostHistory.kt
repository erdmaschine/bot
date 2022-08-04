package erdmaschine.bot.model

import org.jetbrains.exposed.sql.Table

object PostHistories : Table() {
    val guildId = varchar("guildId", 100)
    val channelId = varchar("channelId", 100)
    val sub = varchar("sub", 100)
    val postId = varchar("postId", 100)

    override val primaryKey = PrimaryKey(guildId, channelId, sub, postId)
}
