package erdmaschine.bot.commands

import erdmaschine.bot.model.Fallacies
import erdmaschine.bot.replyError
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload

private const val OPTION_NAME = "name"
private const val OPTION_LINK = "link"

val FallacyCommand = Commands.slash("fallacy", "Post a fallacy referee image")
    .addOption(OptionType.STRING, OPTION_NAME, "Name of the fallacy to post", true, true)
    .addOption(OptionType.STRING, OPTION_LINK, "Link to the message to post this fallacy as reply to")

suspend fun executeFallacyCommand(event: SlashCommandInteractionEvent) {
    val name = event.getOption(OPTION_NAME)?.asString ?: return event.hook.replyError("Fallacy name missing!")
    val filename = "${name}.jpg"
    val data = object {}.javaClass.getResource("/fallacies/$filename")
        ?: throw Exception("Image file [$filename] not found")

    val messageLink = event.getOption(OPTION_LINK)?.asString.orEmpty()
    if (messageLink.isNotBlank()) {
        findMessageLink(event, messageLink).reply("<@${event.user.id}> accuses you of using a fallacy!")
            .addFiles(FileUpload.fromData(data.readBytes(), filename))
            .submit()
        event.hook.sendMessage("Fallacy reply done!").submit()
    } else {
        event.deferReply().submit()
        event.hook.sendFiles(FileUpload.fromData(data.readBytes(), filename)).submit()
    }
}

fun buildFallacyChoices(event: CommandAutoCompleteInteractionEvent): Collection<Choice> =
    when (event.focusedOption.name) {
        OPTION_NAME -> Fallacies
            .filter { (value, description) ->
                "$value $description".contains(event.focusedOption.value)
            }
            .map { (value, description) ->
                val label = "$value: $description"
                val name = when (label.length > 100) {
                    true -> label.take(97) + "..."
                    else -> label
                }
                Choice(name, value)
            }
            .take(25)

        else -> listOf()
    }
