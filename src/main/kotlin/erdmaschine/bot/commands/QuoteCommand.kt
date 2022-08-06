package erdmaschine.bot.commands

import erdmaschine.bot.await
import erdmaschine.bot.forMessage
import erdmaschine.bot.replyError
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_LINK = "link"

val QuoteCommand = Commands.slash("quote", "quote message")
    .addOption(OptionType.STRING, OPTION_LINK, "Link to message", true)

suspend fun executeQuoteCommand(event: SlashCommandInteractionEvent) {
    val interaction = event.reply("Fetching message...").await()

    val messageLink = event.getOption(OPTION_LINK)?.asString.orEmpty()
    val tokenizedLink = messageLink.substringAfter("/channels/", "").split("/")
    if (tokenizedLink.size != 3) {
        return interaction.replyError("Invalid link format!")
    }

    val guildId = tokenizedLink[0]
    val channelId = tokenizedLink[1]
    val messageId = tokenizedLink[2]

    val guild = event.jda.guilds.firstOrNull { it.id == guildId }
    val channel = guild?.getTextChannelById(channelId) ?: guild?.getThreadChannelById(channelId)
    val message = channel?.retrieveMessageById(messageId)?.await()
        ?: return interaction.replyError("No message found at that link!")

    val embed = EmbedBuilder().forMessage(message).build()
    interaction.editOriginal(getQuoteMessage()).await()
    interaction.editOriginalEmbeds(embed).await()
}
