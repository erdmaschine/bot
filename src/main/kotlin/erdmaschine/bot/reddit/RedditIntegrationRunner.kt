package erdmaschine.bot.reddit

import erdmaschine.bot.Env
import erdmaschine.bot.model.Storage
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class RedditIntegrationRunner(
    env: Env,
    private val storage: Storage,
    private val redditFacade: RedditFacade
) : CoroutineScope {

    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val job = Job()
    private val interval = env.redditRunnerInterval.ifEmpty { "60000" }.toLong()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    fun run() = launch {
        while (isActive) {
            delay(interval)
            storage.getSubs().forEach { sub ->
                redditFacade.fetch(sub)
            }
            log.info("Runner finished, delaying for [$interval]ms")
        }
    }
}
