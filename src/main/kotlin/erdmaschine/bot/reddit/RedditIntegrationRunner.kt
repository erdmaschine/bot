package erdmaschine.bot.reddit

import erdmaschine.bot.Env
import erdmaschine.bot.model.Storage
import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class RedditIntegrationRunner(env: Env) : CoroutineScope {

    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val job = Job()
    private val interval = env.redditRunnerInterval.ifEmpty { "60000" }.toLong()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

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

                // TODO Timeout for reposts?
                listingThing.data.children.map { it.data }.forEach { link ->
                    (channel as TextChannel).sendMessageEmbeds(
                        EmbedBuilder()
                            .setAuthor(link.author)
                            // TODO preview as thumbnail
                            .setTitle(link.title, link.url)
                            .setFooter("${sub.sub}/${sub.listing}")
                            .build()
                    )
                        .submit()
                }
            }

            log.info("Runner finished, delaying for [$interval]ms")
        }
    }
}
