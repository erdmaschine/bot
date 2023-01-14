package erdmaschine.bot.commands

import erdmaschine.bot.forMessage
import erdmaschine.bot.model.Fav
import erdmaschine.bot.model.Storage
import erdmaschine.bot.replyError
import erdmaschine.bot.weightedRandom
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger("erdmaschine.bot.commands.FavCommand")!!

private const val OPTION_TAG = "tag"
private const val OPTION_ID = "id"
private const val OPTION_UWU = "uwu"
private const val OPTION_SPONGE = "sponge"

val FavCommand =
    Commands.slash("fav", "Post a random fav, filtered by the given tags or from all favs")
        .addOption(
            OptionType.STRING,
            OPTION_TAG,
            "Limit the favs to only include favs with at least one of these (space-separated) tags"
        )
        .addOption(
            OptionType.STRING,
            OPTION_ID,
            "Post the fav associated with this specific ID"
        )
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

suspend fun executeFavCommand(storage: Storage, event: SlashCommandInteractionEvent) {
    val id = event.getOption(OPTION_ID)?.asString.orEmpty()
    val tags = event.getOption(OPTION_TAG)?.asString?.split(" ").orEmpty()
    val guildIds = event.jda.guilds.map { it.id }

    event.deferReply().submit()

    val candidates = mutableListOf<Fav>()
    if (id.isNotBlank()) {
        val fav = storage.getFav(id)
            ?: return event.hook.replyError("Fav with id [$id] not found")
        candidates.add(fav)
    } else {
        storage
            .getFavs(event.user.id, event.guild?.id, tags)
            .filter { guildIds.contains(it.guildId) }
            .also { candidates.addAll(it) }
    }

    val fav = candidates.weightedRandom()
        ?: return event.hook.replyError("No favs found")

    storage.increaseUsed(fav)

    val guild = event.jda.guilds
        .firstOrNull { it.id == fav.guildId }
        ?: return event.hook.replyError("Guild not found:\n${fav.guildUrl()}", fav.id)
    val channel = guild.getTextChannelById(fav.channelId)
        ?: guild.getThreadChannelById(fav.channelId)
        ?: return event.hook.replyError("Channel not found:\n${fav.channelUrl()}", fav.id)

    val message = retrieveMessageWithErrorHandling(fav, storage, event.hook, channel as TextChannel) ?: return

    val sponge = event.getOption(OPTION_SPONGE)?.asBoolean == true
    val uwu = event.getOption(OPTION_UWU)?.asBoolean == true
    val embed = EmbedBuilder().forMessage(message, fav.id, sponge, uwu).build()

    event.hook.editOriginalEmbeds(embed).submit()

    try {
        val original = event.hook.retrieveOriginal().submit().await()
        original.addReaction(Emoji.fromUnicode("👍")).submit()
        original.addReaction(Emoji.fromUnicode("👎")).submit()
    } catch (exc: Exception) {
        LOG.warn(exc.message, exc)
    }
}
