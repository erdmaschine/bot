package erdmaschine.bot.commands

import dev.minn.jda.ktx.generics.getChannel
import erdmaschine.bot.model.RanickiClips
import erdmaschine.bot.replyError
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_LINK = "link"
private const val OPTION_KEYWORD = "keyword"

val RanickiCommand = Commands.slash("ranicki", "Post an out of context Ranicki clip")
    .addOption(OptionType.STRING, OPTION_KEYWORD, "Keyword to search for clip", true)
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

        message
            .reply(clipUrl)
            .submit()
        event.hook.sendMessage("Ranicki clip reply done!").submit()
    } else {
        event.deferReply().submit()
        event.hook.sendMessage(clipUrl).submit()
    }
}
