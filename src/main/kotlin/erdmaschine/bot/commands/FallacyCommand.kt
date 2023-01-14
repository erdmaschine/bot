package erdmaschine.bot.commands

import dev.minn.jda.ktx.generics.getChannel
import erdmaschine.bot.model.Fallacies
import erdmaschine.bot.replyError
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.FileUpload

private const val OPTION_LINK = "link"

val subCommands = Fallacies.map { (name, description) ->
    SubcommandData(name, description)
        .addOption(OptionType.STRING, OPTION_LINK, "Link to the message to post this fallacy as reply to")
}

val FallacyCommand = Commands.slash("fallacy", "Post a fallacy referee image")
    .addSubcommands(subCommands)

suspend fun executeFallacyCommand(event: SlashCommandInteractionEvent) {
    val filename = "${event.subcommandName}.jpg"
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
