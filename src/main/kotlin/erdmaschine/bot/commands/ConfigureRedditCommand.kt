package erdmaschine.bot.commands

import erdmaschine.bot.await
import erdmaschine.bot.model.Storage
import erdmaschine.bot.reddit.RedditFacade
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.internal.utils.PermissionUtil

private const val COMMAND_ADD_SUB = "add-sub"
private const val COMMAND_REMOVE_SUB = "remove-sub"
private const val OPTION_SUB = "sub"
private const val OPTION_LISTING = "listing"
private const val OPTION_CHANNEL = "channel"
private const val OPTION_NSFW = "nsfw"

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
                channelOption,
                subOption,
                OptionData(
                    OptionType.STRING,
                    OPTION_LISTING,
                    "Subreddit listing type to use (default: 'rising')"
                )
                    .addChoice("top", "top")
                    .addChoice("hot", "hot")
                    .addChoice("rising", "rising")
                    .addChoice("controversial", "controversial")
                    .addChoice("new", "new"),
                OptionData(OptionType.BOOLEAN, OPTION_NSFW, "Allow NSFW posts")
            ),
        SubcommandData(COMMAND_REMOVE_SUB, "Remove a source subreddit")
            .addOptions(channelOption, subOption),
    )

suspend fun executeConfigureRedditCommand(
    storage: Storage,
    redditFacade: RedditFacade,
    event: SlashCommandInteractionEvent
) {
    val channel = event.getOption(OPTION_CHANNEL)?.asMessageChannel ?: throw Exception("Invalid channel type")
    if (!PermissionUtil.checkPermission(channel.permissionContainer, event.member, Permission.MESSAGE_MANAGE)) {
        throw Exception("You are not authorized to configure reddit integration for that channel")
    }

    val sub = event.getOption(OPTION_SUB)?.asString ?: throw Exception("Sub option must be provided")
    val listing = event.getOption(OPTION_LISTING)?.asString?.ifBlank { "rising" } ?: "rising"
    val nsfw = event.getOption(OPTION_NSFW)?.asBoolean ?: false
    var message = "Unknown result"

    when (event.subcommandName) {
        COMMAND_ADD_SUB -> {
            storage.addSub(channel.guild.id, channel.id, sub, listing, nsfw)?.let { subModel ->
                // Prefill the history so there's no initial dump of posts directly after adding
                redditFacade.fetch(listOf(subModel))[subModel.link]?.data?.children?.map { it.data }?.forEach { link ->
                    storage.addPostHistory(subModel.guildId, subModel.channelId, subModel.sub, link.id)
                }
            }

            message = "Done"
        }

        COMMAND_REMOVE_SUB -> {
            storage.removeSub(channel.guild.id, channel.id, sub)
            message = "Done"
        }
    }

    event.interaction
        .reply(message)
        .setEphemeral(true)
        .await()
}
