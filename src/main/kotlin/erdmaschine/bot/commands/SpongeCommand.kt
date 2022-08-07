package erdmaschine.bot.commands

import erdmaschine.bot.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_TEXT = "text"
private const val OPTION_UWU = "uwu"

val SpongeCommand = Commands.slash("sponge", "sPoNgEbObIfY a TeXt")
    .addOption(OptionType.STRING, OPTION_TEXT, "Text to be used", true)
    .addOption(
        OptionType.BOOLEAN,
        OPTION_UWU,
        "Uwuwify as well"
    )

suspend fun executeSpongeCommand(event: SlashCommandInteractionEvent) {
    val text = event.getOption(OPTION_TEXT)?.asString?.spongeify() ?: throw Exception("Text must be provided")

    if (event.getOption(OPTION_UWU)?.asBoolean == true) {
        text.uwuify()
    }

    event.reply(text).await()
}

fun String.spongeify() = this.lowercase().mapIndexed { idx, char ->
    when (idx % 2) {
        1 -> char.uppercaseChar()
        else -> char
    }
}.joinToString("")
