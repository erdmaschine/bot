package erdmaschine.bot.commands

import erdmaschine.bot.model.RanickiClips
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_LINK = "link"
private const val OPTION_KEYWORD = "keyword"

val RanickiCommand = Commands.slash("ranicki", "Post an out of context Ranicki clip")
    .addOption(OptionType.STRING, OPTION_KEYWORD, "Keyword to search for clip", true, true)
    .addOption(OptionType.STRING, OPTION_LINK, "Link to the message to post the clip as reply to")

suspend fun executeRanickiCommand(event: SlashCommandInteractionEvent) {
    val keyword = event.getOption(OPTION_KEYWORD)?.asString
        ?: throw Exception("Keyword option is required")

    val clipUrl = RanickiClips
        .filter { entry -> entry.key.contains(keyword) }
        .map { it.value }
        .firstOrNull()
        ?: throw Exception("No clip found for keyword")

    val messageLink = event.getOption(OPTION_LINK)?.asString.orEmpty()
    if (messageLink.isNotBlank()) {
        findMessageLink(event, messageLink)
            .reply(clipUrl)
            .submit()
        event.hook.sendMessage("Ranicki clip reply done!").submit()
    } else {
        event.deferReply().submit()
        event.hook.sendMessage(clipUrl).submit()
    }
}

fun buildRanickiChoices(event: CommandAutoCompleteInteractionEvent): Collection<Command.Choice> =
    when (event.focusedOption.name) {
        OPTION_KEYWORD -> RanickiClips.keys.flatten()
            .filter { it.contains(event.focusedOption.value) }
            .take(25)
            .map { Command.Choice(it, it) }

        else -> listOf()
    }
