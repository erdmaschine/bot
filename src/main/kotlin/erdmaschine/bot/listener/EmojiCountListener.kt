package erdmaschine.bot.listener

import erdmaschine.bot.model.Storage
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

private val EMOJI_REGEX = Regex("<a?:(\\w+):\\d+>")

class EmojiCountListener(
    private val storage: Storage
) : ListenerAdapter() {

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) = runBlocking {
        val validEmojis = event.guild.emojiCache.map { it.name }
        val reactionEmoji = event.emoji.name

        if (validEmojis.contains(reactionEmoji)) {
            storage.updateEmojiUsage(event.guild.id, reactionEmoji)
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) = runBlocking {
        val validEmojis = event.guild.emojiCache.map { it.name }

        EMOJI_REGEX.findAll(event.message.contentRaw)
            .mapNotNull { it.groups[1]?.value }
            .filter { validEmojis.contains(it) }
            .forEach { emoji ->
                storage.updateEmojiUsage(event.guild.id, emoji)
            }
    }
}
