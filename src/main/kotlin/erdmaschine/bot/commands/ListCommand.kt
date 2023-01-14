package erdmaschine.bot.commands

import erdmaschine.bot.model.Storage
import erdmaschine.bot.replyError
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.awt.Color

private const val OPTION_TAG = "tag"
private const val OPTION_GUILD = "server"

val ListCommand = Commands.slash("list", "List amount of favs per tag")
    .addOption(
        OptionType.STRING,
        OPTION_TAG,
        "Limit the listed counts to favs with at least one of these (space-separated) tags"
    )
    .addOption(
        OptionType.STRING,
        OPTION_GUILD,
        "Limit the listed counts to favs from a single server (provided the server id)"
    )

suspend fun executeListCommand(storage: Storage, event: SlashCommandInteractionEvent) {
    event.deferReply().submit()

    val tags = event.getOption(OPTION_TAG)?.asString.orEmpty().split(" ").filter { it.isNotBlank() }
    val guildId = event.getOption(OPTION_GUILD)?.asString ?: event.guild?.id
    val favs = storage.getFavs(event.user.id, guildId, tags)

    if (favs.isEmpty()) {
        return event.hook.replyError("No favs found")
    }

    val tagCount = mutableMapOf<String, Int>()
    favs
        .forEach { fav ->
            fav.tags.forEach {
                val count = tagCount[it] ?: 0
                tagCount[it] = count + 1
            }
        }

    event.hook.editOriginal("Listing total of ${tagCount.size} tags").submit()

    tagCount
        .entries
        .sortedBy { it.key }
        .chunked(25)
        .chunked(10)
        .forEach { messageEntries ->
            val embeds = mutableListOf<MessageEmbed>()
            messageEntries.forEach { fields ->
                val builder = EmbedBuilder().setColor(Color(20, 150, 115))
                fields.forEach { builder.addField(it.key, it.value.toString(), true) }
                embeds.add(builder.build())
            }
            event
                .channel
                .sendMessage(MessageCreateBuilder().setEmbeds(embeds).build())
                .submit()
        }
}
