package erdmaschine.bot.reddit

import erdmaschine.bot.Env
import erdmaschine.bot.await
import erdmaschine.bot.model.Storage
import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class RedditIntegrationRunner(env: Env) : CoroutineScope {

    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val job = Job()
    private val interval = env.redditRunnerInterval.ifEmpty { "60000" }.toLong()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private val history = HashSet<String>()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    fun run(storage: Storage, redditFacade: RedditFacade, jda: JDA) = launch {
        while (isActive) {
            delay(interval)
            storage.getSubs().forEach { sub ->
                val listingThing = redditFacade.fetch(sub)
                val channel = jda.getGuildById(sub.guildId)?.getGuildChannelById(sub.channelId)
                if (channel == null) {
                    log.warn("Invalid Sub[${sub.sub}/${sub.listing}] on Guild[${sub.guildId}] in Channel[${sub.channelId}]")
                    return@forEach
                }

                if (channel.type != ChannelType.TEXT) {
                    return@forEach
                }

                val link = listingThing.data.children
                    .map { it.data }
                    .firstOrNull { !history.contains(it.id) }
                    ?: return@forEach

                val embed = EmbedBuilder()
                    .setAuthor(link.author)
                    .setTitle(link.title, "https://www.reddit.com${link.permalink}")
                    .setFooter("${sub.sub}/${sub.listing}")
                    .setTimestamp(Date((link.created * 1000).toLong()).toInstant())

                link.preview?.images
                    ?.firstOrNull()
                    ?.resolutions
                    ?.firstOrNull { it.width in 300..600 }
                    ?.url
                    ?.let { embed.setImage(it.replace("&amp;", "&")) }

                history.add(link.id)

                (channel as TextChannel).sendMessageEmbeds(embed.build()).await()
            }

            log.info("Runner finished, delaying for [$interval]ms")
        }
    }
}
