package erdmaschine.bot.commands

import erdmaschine.bot.model.Storage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.awt.Color

private const val OPTION_TAG = "tag"

val StatsCommand = Commands.slash("stats", "Display your fav stats")
    .addOption(
        OptionType.STRING,
        OPTION_TAG,
        "Limit the listed counts to favs with at least one of these (space-separated) tags"
    )

suspend fun executeStatsCommand(storage: Storage, event: SlashCommandInteractionEvent) {
    event.deferReply().submit()

    val tags = event.getOption(OPTION_TAG)?.asString.orEmpty().split(" ").filter { it.isNotBlank() }
    val favs = storage.getFavs(event.user.id, event.guild?.id, tags)

    event.hook.editOriginalEmbeds(
        EmbedBuilder()
            .setTitle("${event.user.name} Fav Stats")
            .setColor(Color(20, 150, 115))
            .writeStats(favs, event.jda)
            .build()
    ).submit()
}
