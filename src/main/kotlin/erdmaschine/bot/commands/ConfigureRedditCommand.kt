package erdmaschine.bot.commands

import erdmaschine.bot.await
import erdmaschine.bot.model.Storage
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

private const val COMMAND_ADD_SUB = "add-sub"
private const val COMMAND_REMOVE_SUB = "remove-sub"
private const val OPTION_SUB = "sub"
private const val OPTION_LISTING = "listing"
private const val OPTION_CHANNEL = "channel"

val subOption = OptionData(
    OptionType.STRING,
    OPTION_SUB,
    "Pure name of the sub (e.g. 'funny' or 'todayilearned')",
    true
)

val channelOption = OptionData(
    OptionType.CHANNEL,
    OPTION_CHANNEL,
    "The channel to configure for",
    true
)

val ConfigureRedditCommand = Commands.slash("config-reddit", "Configure reddit integration")
    .addSubcommands(
        SubcommandData(COMMAND_ADD_SUB, "Add a new source subreddit")
            .addOptions(
                channelOption, subOption,
                OptionData(
                    OptionType.STRING,
                    OPTION_LISTING,
                    "Subreddit listing type to use",
                    true
                )
                    .addChoice("top", "top")
                    .addChoice("hot", "hot")
                    .addChoice("rising", "rising")
                    .addChoice("controversial", "controversial")
                    .addChoice("new", "new")
            ),
        SubcommandData(COMMAND_REMOVE_SUB, "Remove a source subreddit")
            .addOptions(channelOption, subOption),
    )

suspend fun executeConfigureRedditCommand(storage: Storage, event: SlashCommandInteractionEvent) {
    val channel = event.getOption(OPTION_CHANNEL)?.asMessageChannel ?: throw Exception("Channel not valid")
    val owner = channel.guild.retrieveOwner().await()
    if (owner.user != event.user) {
        throw Exception("You are not authorized to configure reddit integration for that channel")
    }

    val sub = event.getOption(OPTION_SUB)?.asString ?: throw Exception("Sub option must be provided")
    val listing = event.getOption(OPTION_LISTING)?.asString
    var message = "Unknown result"

    when (event.subcommandName) {
        COMMAND_ADD_SUB -> {
            if (listing == null) {
                throw Exception("Listing option must be provided")
            }
            storage.addSub(channel.guild.id, channel.id, sub, listing)
            message = "Sub[$sub/$listing] was added!"
        }

        COMMAND_REMOVE_SUB -> {
            storage.removeSub(channel.guild.id, channel.id, sub)
            message = "Sub[$sub] was removed!"
        }
    }

    event.interaction
        .reply(message)
        .setEphemeral(true)
        .await()
}
