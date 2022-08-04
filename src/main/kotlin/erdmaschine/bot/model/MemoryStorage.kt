package erdmaschine.bot.model

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

val autoIncrementId = AtomicInteger(1)

class MemoryStorage : Storage {
    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val favs = HashMap<String, Fav>()
    private val redditSubs = HashSet<Sub>()

    override suspend fun saveNewFav(
        userId: String,
        guildId: String,
        channelId: String,
        messageId: String,
        authorId: String
    ): String {
        log.info("Creating fav for User[$userId] Guild[$guildId] Channel[$channelId] Message[$messageId]")
        val fav = Fav(
            autoIncrementId.getAndIncrement().toString(),
            userId,
            guildId,
            channelId,
            messageId,
            authorId,
            mutableListOf(),
            0,
            0,
        )
        favs[fav.id] = fav
        return fav.id
    }

    override suspend fun getFavs(userId: String?, guildId: String?, tags: Collection<String>): List<Fav> {
        log.info("Getting favs for User[$userId] Guild[$guildId] Tags$tags")
        return favs.values
            .filter { userId == null || it.userId == userId }
            .filter { guildId == null || it.guildId == guildId }
            .filter { tags.isEmpty() || it.tags.any { tag -> tags.contains(tag.lowercase()) } }
    }

    override suspend fun removeFav(favId: String) {
        log.info("Removing Fav[$favId]")
        favs.remove(favId)
    }

    override suspend fun writeTags(favId: String, tags: Collection<String>) {
        log.info("Setting tags of Fav[$favId] to $tags")
        val fav = getFav(favId) ?: return
        favs[fav.id] = Fav(
            fav.id,
            fav.userId,
            fav.guildId,
            fav.channelId,
            fav.messageId,
            fav.authorId,
            tags,
            fav.used,
            fav.votes
        )
    }

    override suspend fun getFav(favId: String): Fav? {
        log.info("Fetching Fav[$favId]")
        return favs[favId]
    }

    override suspend fun increaseUsed(fav: Fav) {
        favs[fav.id] = Fav(
            fav.id,
            fav.userId,
            fav.guildId,
            fav.channelId,
            fav.messageId,
            fav.authorId,
            fav.tags,
            fav.used + 1,
            fav.votes
        )
    }

    override suspend fun upvote(fav: Fav) {
        log.info("Upvoting Fav[${fav.id}]")
        favs[fav.id] = Fav(
            fav.id,
            fav.userId,
            fav.guildId,
            fav.channelId,
            fav.messageId,
            fav.authorId,
            fav.tags,
            fav.used,
            fav.votes + 1
        )
    }

    override suspend fun downvote(fav: Fav) {
        log.info("Downvoting Fav[${fav.id}]")
        favs[fav.id] = Fav(
            fav.id,
            fav.userId,
            fav.guildId,
            fav.channelId,
            fav.messageId,
            fav.authorId,
            fav.tags,
            fav.used - 1,
            fav.votes
        )
    }

    override suspend fun addSub(guildId: String, channelId: String, sub: String, listing: String, nsfw: Boolean) {
        log.info("Adding new Sub[$sub/$listing] for Guild[$guildId] in Channel[$channelId]")
        redditSubs.add(Sub(guildId, channelId, sub, listing, nsfw))
    }

    override suspend fun removeSub(guildId: String, channelId: String, sub: String) {
        log.info("Removing Sub[$sub] from Guild[${guildId}] in Channel[$channelId]")
        redditSubs.removeIf { it.guildId == guildId && it.channelId == channelId && it.sub == sub }
    }

    override suspend fun getSubs(): Collection<Sub> {
        return redditSubs
    }
}
