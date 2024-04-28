package erdmaschine.bot.commands

import erdmaschine.bot.model.XkcdComics
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_LINK = "link"
private const val OPTION_KEYWORD = "keyword"

val XkcdCommand = Commands.slash("xkcd", "Post an XKCD Comic")
    .addOption(OptionType.STRING, OPTION_KEYWORD, "Keyword to search for comic", true, true)
    .addOption(OptionType.STRING, OPTION_LINK, "Link to the message to post the comic as reply to")

suspend fun executeXkcdCommand(event: SlashCommandInteractionEvent) {
    val keyword = event.getOption(OPTION_KEYWORD)?.asString
        ?: throw Exception("Keyword option is required")

    val xkcdUrl = XkcdComics
        .filter { entry -> entry.key.contains(keyword) }
        .map { it.value }
        .firstOrNull()
        ?: throw Exception("No comic found for keyword")

    val messageLink = event.getOption(OPTION_LINK)?.asString.orEmpty()
    if (messageLink.isNotBlank()) {
        findMessageLink(event, messageLink)
            .reply(xkcdUrl)
            .submit()
        event.hook.sendMessage("XKCD comic reply done!").submit()
    } else {
        event.deferReply().submit()
        event.hook.sendMessage(xkcdUrl).submit()
    }
}

fun buildXkcdChoices(event: CommandAutoCompleteInteractionEvent): Collection<Command.Choice> =
    when (event.focusedOption.name) {
        OPTION_KEYWORD -> XkcdComics.keys.flatten()
            .filter { it.contains(event.focusedOption.value) }
            .take(25)
            .map { Command.Choice(it, it) }

        else -> listOf()
    }

