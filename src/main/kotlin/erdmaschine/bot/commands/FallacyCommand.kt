package erdmaschine.bot.commands

import dev.minn.jda.ktx.generics.getChannel
import erdmaschine.bot.model.Fallacies
import erdmaschine.bot.replyError
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
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
        ?: throw Exception("Unknown subcommand or image file not found")

    val messageLink = event.getOption(OPTION_LINK)?.asString.orEmpty()
    if (messageLink.isNotBlank()) {
        event.deferReply().setEphemeral(true).submit()

        val tokenizedLink = messageLink.substringAfter("/channels/", "").split("/")
        if (tokenizedLink.size != 3) {
            return event.hook.replyError("Invalid link format!")
        }

        val guildId = tokenizedLink[0]
        val channelId = tokenizedLink[1]
        val messageId = tokenizedLink[2]

        val guild = event.jda.guilds.firstOrNull { it.id == guildId }
        val channel = guild?.getChannel<GuildMessageChannel>(channelId)

        val message = channel?.retrieveMessageById(messageId)?.submit()?.await()
            ?: return event.hook.replyError("No message found at that link!")

        message.reply("<@${event.user.id}> accuses you of using a fallacy!")
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
        OPTION_NAME -> Fallacies.map { (value, description) ->
            val label = "$value: $description"
            val name = when (label.length > 100) {
                true -> label.take(97) + "..."
                else -> label
            }
            Choice(name, value)
        }

        else -> listOf()
    }
