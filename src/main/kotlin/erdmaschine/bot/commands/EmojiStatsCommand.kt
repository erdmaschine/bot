package erdmaschine.bot.commands

import erdmaschine.bot.model.Storage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.awt.Color

val EmojiStatsCommand = Commands.slash("emojistats", "Display server emoji usage stats")

suspend fun executeEmojiStats(storage: Storage, event: SlashCommandInteractionEvent) {
    event.deferReply().submit()

    val builder = EmbedBuilder()
        .setTitle("Emoji Usage Stats")
        .setColor(Color(115, 150, 20))

    storage.getEmojiUsages(event.guild?.id).forEach { usage ->
        builder.addField(usage.emojiName, "${usage.used}", true)
    }

    event.hook.editOriginalEmbeds(builder.build()).submit()
}
