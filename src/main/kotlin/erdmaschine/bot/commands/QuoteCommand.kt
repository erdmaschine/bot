package erdmaschine.bot.commands

import erdmaschine.bot.forMessage
import erdmaschine.bot.replyError
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_LINK = "link"
private const val OPTION_UWU = "uwu"
private const val OPTION_SPONGE = "sponge"

val QuoteCommand = Commands.slash("quote", "quote message")
    .addOption(OptionType.STRING, OPTION_LINK, "Link to message", true)
    .addOption(
        OptionType.BOOLEAN,
        OPTION_UWU,
        "Uwuwify the fav"
    )
    .addOption(
        OptionType.BOOLEAN,
        OPTION_SPONGE,
        "Spongeifiy the fav"
    )

suspend fun executeQuoteCommand(event: SlashCommandInteractionEvent) {
    event.deferReply().submit()

    val messageLink = event.getOption(OPTION_LINK)?.asString.orEmpty()
    val tokenizedLink = messageLink.substringAfter("/channels/", "").split("/")
    if (tokenizedLink.size != 3) {
        return event.hook.replyError("Invalid link format!")
    }

    val guildId = tokenizedLink[0]
    val channelId = tokenizedLink[1]
    val messageId = tokenizedLink[2]

    val guild = event.jda.guilds.firstOrNull { it.id == guildId }
    val channel = guild?.getTextChannelById(channelId) ?: guild?.getThreadChannelById(channelId)
    val message = channel?.retrieveMessageById(messageId)?.submit()?.await()
        ?: return event.hook.replyError("No message found at that link!")

    val sponge = event.getOption(OPTION_SPONGE)?.asBoolean == true
    val uwu = event.getOption(OPTION_UWU)?.asBoolean == true

    val embed = EmbedBuilder().forMessage(message, null, sponge, uwu).build()

    event.hook.editOriginalEmbeds(embed).submit()
}
