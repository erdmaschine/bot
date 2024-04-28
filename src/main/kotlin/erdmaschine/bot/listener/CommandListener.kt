package erdmaschine.bot.listener

import erdmaschine.bot.Env
import erdmaschine.bot.commands.*
import erdmaschine.bot.model.Storage
import erdmaschine.bot.reddit.RedditFacade
import erdmaschine.bot.replyError
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory


private val slashCommandData = listOf(
    ListCommand,
    FavCommand,
    QuoteCommand,
    StatsCommand,
    GuildStatsCommand,
    MysteryFavCommand,
    ConfigureRedditCommand,
    SpongeCommand,
    UwuCommand,
    FallacyCommand,
    RanickiCommand,
    XkcdCommand,
    EmojiStatsCommand,
    PropagandaCommand,
    MiscCommand,
)

class CommandListener(
    private val storage: Storage,
    private val redditFacade: RedditFacade
) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    suspend fun initCommands(jda: JDA, env: Env) {
        if (env.deployCommandsGobal == "true") {
            jda.guilds.forEach { guild ->
                guild.retrieveCommands().submit().await().forEach { command ->
                    try {
                        log.info("Removing command [{}] from guild [{}]", command.name, guild)
                        guild.deleteCommandById(command.id).submit()
                    } catch (e: Exception) {
                        log.warn("Could not remove guild command: ${e.message}", e)
                    }
                }
            }

            log.info("Initializing commands globally")
            jda.updateCommands().addCommands(slashCommandData).submit()
        } else {
            jda.guilds.forEach {
                log.info("Initializing commands on [$it]")
                it.updateCommands().addCommands(slashCommandData).submit()
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = runBlocking {
        try {
            when (event.name) {
                ListCommand.name -> executeListCommand(storage, event)
                FavCommand.name -> executeFavCommand(storage, event)
                QuoteCommand.name -> executeQuoteCommand(event)
                StatsCommand.name -> executeStatsCommand(storage, event)
                GuildStatsCommand.name -> executeGuildStats(storage, event)
                MysteryFavCommand.name -> executeMysteryFavCommand(storage, event)
                ConfigureRedditCommand.name -> executeConfigureRedditCommand(storage, redditFacade, event)
                SpongeCommand.name -> executeSpongeCommand(event)
                UwuCommand.name -> executeUwuCommand(event)
                FallacyCommand.name -> executeFallacyCommand(event)
                RanickiCommand.name -> executeRanickiCommand(event)
                XkcdCommand.name -> executeXkcdCommand(event)
                EmojiStatsCommand.name -> executeEmojiStats(storage, event)
                PropagandaCommand.name -> executePropagandaCommand(event)
                MiscCommand.name -> executeMiscCommand(event)
                else -> Unit
            }
        } catch (e: Exception) {
            val interaction = when (event.isAcknowledged) {
                true -> event.hook
                else -> event.reply("Whoopsie (╯°□°）╯︵ ┻━┻").setEphemeral(true).submit().await()
            }
            interaction.replyError(e.message ?: "Unknown error")
            log.error(e.message, e)
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        try {
            val choices = when (event.name) {
                FallacyCommand.name -> buildFallacyChoices(event)
                RanickiCommand.name -> buildRanickiChoices(event)
                XkcdCommand.name    -> buildXkcdChoices(event)
                PropagandaCommand.name -> buildPropagandaChoices(event)
                else -> listOf()
            }
            event.replyChoices(choices).queue()
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }
}

