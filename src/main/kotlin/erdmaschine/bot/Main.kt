package erdmaschine.bot

import erdmaschine.bot.listener.CommandListener
import erdmaschine.bot.listener.MessageListener
import erdmaschine.bot.listener.ReactionListener
import erdmaschine.bot.model.Storage
import erdmaschine.bot.reddit.RedditFacade
import erdmaschine.bot.reddit.RedditIntegrationRunner
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory
import java.time.Clock
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() = runBlocking {
    val env = Env()
    val log = LoggerFactory.getLogger("erdmaschine.bot.Main")!!
    val redditIntegrationRunner = RedditIntegrationRunner(env)

    try {
        log.info("Starting up erdmaschine-bot")

        val storage = Storage(env)
        val redditFacade = RedditFacade(env, Clock.systemDefaultZone())
        val commandListener = CommandListener(storage, redditFacade)
        val reactionListener = ReactionListener(storage)
        val messageListener = MessageListener(storage)

        val jda = JDABuilder.createDefault(env.authToken, GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(reactionListener, messageListener, commandListener)
            .build()

        jda.awaitReady()

        redditIntegrationRunner.run(storage, redditFacade, jda)

        commandListener.initCommands(jda, env)

        jda.presence.setPresence(Activity.watching("out for hot takes"), false)

        log.info("Ready for action")
    } catch (e: Exception) {
        redditIntegrationRunner.stop()
        log.error(e.message, e)
    }
}
