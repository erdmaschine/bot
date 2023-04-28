package erdmaschine.bot.commands

import erdmaschine.bot.model.Propaganda
import erdmaschine.bot.replyError
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload

private const val OPTION_NAME = "name"
private const val OPTION_LINK = "link"

val PropagandaCommand = Commands.slash("propaganda", "Post a propaganda image")
    .addOption(OptionType.STRING, OPTION_NAME, "Name of the image to post", true, true)
    .addOption(OptionType.STRING, OPTION_LINK, "Link to the message to post this image as reply to")

suspend fun executePropagandaCommand(event: SlashCommandInteractionEvent) {
    val name = event.getOption(OPTION_NAME)?.asString ?: return event.hook.replyError("No name provided!")

    val filename = Propaganda[name]
        ?: return event.hook.replyError("No image found for name [$name]")

    val data = object {}.javaClass.getResource("/propaganda/$filename")
        ?: throw Exception("Image file [$filename] found")

    val messageLink = event.getOption(OPTION_LINK)?.asString.orEmpty()
    if (messageLink.isNotBlank()) {
        findMessageLink(event, messageLink)
            .replyFiles(FileUpload.fromData(data.readBytes(), filename))
            .submit()
        event.hook.sendMessage("Propaganda image reply done!").submit()
    } else {
        event.deferReply().submit()
        event.hook.sendFiles(FileUpload.fromData(data.readBytes(), filename)).submit()
    }
}

fun buildPropagandaChoices(event: CommandAutoCompleteInteractionEvent): Collection<Choice> =
    when (event.focusedOption.name) {
        OPTION_NAME -> Propaganda
            .filter { (key, _) -> key.contains(event.focusedOption.value) }
            .map { (key, _) -> Choice(key, key) }
            .take(25)
            .toList()

        else -> listOf()
    }
