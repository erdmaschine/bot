package erdmaschine.bot.commands

import erdmaschine.bot.model.Storage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.awt.Color

val GuildStatsCommand = Commands.slash("serverstats", "Display server-wide fav stats")

suspend fun executeGuildStats(storage: Storage, event: SlashCommandInteractionEvent) {
    event.deferReply().submit()

    val favs = storage.getFavs(null, event.guild?.id, emptyList())
    event.hook.editOriginalEmbeds(
        EmbedBuilder()
            .setTitle("Server Stats")
            .setColor(Color(20, 150, 115))
            .writeStats(favs, event.jda)
            .build()
    ).submit()
}
