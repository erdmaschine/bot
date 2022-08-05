package erdmaschine.bot.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PostHistory : Table() {
    val guildId = varchar("guildId", 100)
    val channelId = varchar("channelId", 100)
    val sub = varchar("sub", 100)
    val postId = varchar("postId", 100)
    val lastSeen = timestamp("lastSeen")

    override val primaryKey = PrimaryKey(guildId, channelId, sub, postId)
}
