package erdmaschine.bot

import io.github.cdimascio.dotenv.Dotenv

class Env {
    private val env = Dotenv.configure().ignoreIfMissing().load()!!

    val dbUrl: String
        get() = get("DB_URL")

    val dbUser: String
        get() = get("DB_USER")

    val dbPassword: String
        get() = get("DB_PASSWORD")

    val authToken: String
        get() = get("AUTH_TOKEN")

    val deployCommandsGobal: String
        get() = get("DEPLOY_COMMANDS_GLOBAL")

    val redditClientId: String
        get() = get("REDDIT_CLIENT_ID")

    val redditClientSecret: String
        get() = get("REDDIT_CLIENT_SECRET")

    val redditRunnerInterval: String
        get() = get("REDDIT_RUNNER_INTERVAL")

    private fun get(name: String) = env[name].orEmpty()
}
