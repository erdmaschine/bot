package erdmaschine.bot.reddit

import erdmaschine.bot.Env
import erdmaschine.bot.await
import erdmaschine.bot.model.Storage
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RedditIntegrationRunner(private val env: Env) {

    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val interval = env.redditRunnerInterval.ifEmpty { "60000" }.toLong()
    private val history = mutableMapOf<String, MutableList<String>>()

    fun run(storage: Storage, redditFacade: RedditFacade, jda: JDA) {
        scheduler.scheduleAtFixedRate(Runnable {
            runBlocking {
                execute(storage, redditFacade, jda)
            }
        }, interval, interval, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        scheduler.shutdown()
    }

    private suspend fun execute(storage: Storage, redditFacade: RedditFacade, jda: JDA) {
        val subs = storage.getSubs()

        if (subs.isEmpty()) {
            return
        }

        log.info("Starting runner fetch for [${subs.size}] subs")
        val listingThings = redditFacade.fetch(subs)

        subs.forEach { sub ->
            val channel = jda.getGuildById(sub.guildId)?.getGuildChannelById(sub.channelId)
            if (channel == null) {
                log.warn("Could not find channel for $sub")
                return@forEach
            }

            if (channel !is MessageChannel) {
                return@forEach
            }

            val listingThing = listingThings[sub.link] ?: return@forEach

            val link = listingThing.data.children
                .map { it.data }
                .filter { !history.contains(it.id) }
                .randomOrNull()
                ?: return@forEach

            val embed = EmbedBuilder()
                .setAuthor(link.author)
                .setTitle(link.title.take(200), "https://www.reddit.com${link.permalink}")
                .setFooter(sub.link)
                .setTimestamp(Date((link.created * 1000).toLong()).toInstant())
                .setDescription(
                    when (link.is_self) {
                        true -> link.selftext.take(200)
                        else -> link.url
                    }
                )

            link.preview?.images
                ?.firstOrNull()
                ?.resolutions
                ?.firstOrNull { it.width in 300..600 }
                ?.url
                ?.let { embed.setImage(it.replace("&amp;", "&")) }

            val channelHistory = history[sub.channelKey] ?: mutableListOf()

            if (channelHistory.size > env.redditRunnerHistorySize.ifEmpty { "25" }.toInt()) {
                channelHistory.removeFirst()
            }

            channelHistory.add(link.id)
            history[sub.channelKey] = channelHistory

            (channel as MessageChannel).sendMessageEmbeds(embed.build()).await()

            log.info("Runner finished, next run in [$interval]ms")
        }
    }
}
