package erdmaschine.bot.commands

import erdmaschine.bot.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_TEXT = "text"

val SpongeCommand = Commands.slash("sponge", "sPoNgEbObIfY a TeXt")
    .addOption(OptionType.STRING, OPTION_TEXT, "Text to be used", true)

suspend fun executeSpongeCommand(event: SlashCommandInteractionEvent) {
    val text = event.getOption(OPTION_TEXT)?.asString ?: throw Exception("Text must be provided")
    val spongified = text.lowercase().mapIndexed { idx, char ->
        when (idx % 2) {
            1 -> char.uppercaseChar()
            else -> char
        }
    }.joinToString("")
    event.reply(spongified).await()
}
