package erdmaschine.bot.reddit

import erdmaschine.bot.Env
import erdmaschine.bot.model.Storage
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RedditIntegrationRunner(env: Env) {

    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val interval = env.redditRunnerInterval.ifEmpty { "60000" }.toLong()

    fun run(storage: Storage, redditFacade: RedditFacade, jda: JDA) {
        scheduler.scheduleAtFixedRate(Runnable {
            runBlocking {
                cleanupPostHistories(storage)
                postSubs(storage, redditFacade, jda)
            }
        }, interval, interval, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        scheduler.shutdown()
    }

    private suspend fun postSubs(storage: Storage, redditFacade: RedditFacade, jda: JDA) {
        val subs = storage.getSubs()

        if (subs.isEmpty()) {
            return
        }

        try {
            log.info("Starting runner fetch for [${subs.size}] subs")
            val listingThings = redditFacade.fetch(subs)

            subs.forEach { sub ->
                val channel = jda.getGuildById(sub.guildId)?.getGuildChannelById(sub.channelId)
                if (channel == null) {
                    log.warn("Could not find channel for $sub")
                    storage.removeSub(sub.guildId, sub.channelId, sub.sub)
                    return@forEach
                }

                if (channel !is MessageChannel) {
                    storage.removeSub(sub.guildId, sub.channelId, sub.sub)
                    return@forEach
                }

                val listingThing = listingThings[sub.link] ?: return@forEach

                val link = listingThing.data.children
                    .map { it.data }
                    .filter { !storage.isInPostHistory(sub.guildId, sub.channelId, sub.sub, it.id) }
                    .randomOrNull()
                    ?: return@forEach

                val embed = EmbedBuilder()
                    .setAuthor(link.author)
                    .setTitle(link.title.take(200), "https://www.reddit.com${link.permalink}")
                    .setColor(Color(240, 100, 60))
                    .setFooter(sub.link)
                    .setTimestamp(Date((link.created * 1000).toLong()).toInstant())
                    .setDescription(
                        when (link.is_self) {
                            true -> link.selftext.take(200)
                            else -> link.url
                        }
                    )

                val resolutions = link.preview?.images
                    ?.firstOrNull()
                    ?.resolutions
                    .orEmpty()
                if (!resolutions.isEmpty()) {
                    val imageUrl = resolutions
                        .maxBy { it.width }
                        .url
                        .replace("&amp;", "&")
                    embed.setImage(imageUrl)
                }

                storage.addPostHistory(sub.guildId, sub.channelId, sub.sub, link.id)

                (channel as MessageChannel).sendMessageEmbeds(embed.build()).submit()
            }

            log.info("Runner finished, next run in [$interval]ms")
        } catch (exc: Exception) {
            log.error(exc.message, exc)
        }
    }

    private suspend fun cleanupPostHistories(storage: Storage) {
        try {
            storage.cleanupPostHistory()
        } catch (exc: Exception) {
            log.error(exc.message, exc)
        }
    }
}
