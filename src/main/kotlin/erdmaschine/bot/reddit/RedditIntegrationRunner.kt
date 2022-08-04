package erdmaschine.bot.reddit

import erdmaschine.bot.Env
import erdmaschine.bot.await
import erdmaschine.bot.model.Storage
import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class RedditIntegrationRunner(private val env: Env) : CoroutineScope {

    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val job = Job()
    private val interval = env.redditRunnerInterval.ifEmpty { "60000" }.toLong()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private val history = mutableMapOf<String, MutableList<String>>()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    fun run(storage: Storage, redditFacade: RedditFacade, jda: JDA) = launch {
        while (isActive) {
            delay(interval)

            log.info("Starting runner")

            val subs = storage.getSubs()
            val listingThings = redditFacade.fetch(subs)

            subs.forEach { sub ->
                val channel = jda.getGuildById(sub.guildId)?.getGuildChannelById(sub.channelId)
                if (channel == null) {
                    log.warn("Could not channel for $sub")
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
                    .setTitle(link.title, "https://www.reddit.com${link.permalink}")
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
            }

            log.info("Runner finished, delaying for [$interval]ms")
        }
    }
}
