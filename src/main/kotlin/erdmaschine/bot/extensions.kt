package erdmaschine.bot

import erdmaschine.bot.commands.spongeify
import erdmaschine.bot.commands.uwuify
import erdmaschine.bot.model.Fav
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import org.apache.commons.rng.sampling.DiscreteProbabilityCollectionSampler
import org.apache.commons.rng.simple.RandomSource
import java.awt.Color

val RNG = RandomSource.JDK.create()!!

fun InteractionHook.replyError(message: String, favId: String? = null) {
    this.editOriginal("Whoopsie (╯°□°）╯︵ ┻━┻").submit()
    this.editOriginalEmbeds(
        EmbedBuilder()
            .setDescription(message)
            .setFooter(favId)
            .setColor(Color(150, 25, 25))
            .build()
    )
        .submit()
}

fun Collection<Fav>.weightedRandom(): Fav? {
    if (this.isEmpty()) {
        return null
    }

    val map = this.associateWith { 1 / (it.used.toDouble() + 1) }
    return try {
        DiscreteProbabilityCollectionSampler(RNG, map).sample()
    } catch (e: IndexOutOfBoundsException) {
        null
    }
}

fun EmbedBuilder.forMessage(
    message: Message,
    favId: String? = "",
    sponge: Boolean? = false,
    uwu: Boolean? = false
): EmbedBuilder = with(this) {
    val channelName = message.channel.name
    setAuthor("${message.author.name} in #$channelName", message.jumpUrl, message.author.effectiveAvatarUrl)

    var text = message.contentRaw

    if (sponge == true) {
        text = text.spongeify()
    }
    if (uwu == true) {
        text = text.uwuify()
    }

    setColor(Color(45, 80, 50))
    setDescription(text)
    setTimestamp(message.timeCreated)

    val embedImageUrl = message.attachments
        .firstOrNull { it.isImage }
        ?.proxyUrl
        ?.also { setImage(it) }

    message.attachments
        .filter { embedImageUrl != null && it.proxyUrl != embedImageUrl }
        .forEach {
            var description = ""
            if (it.description != null) {
                description = "${it.description}: "
            }
            appendDescription("\n$description${it.proxyUrl}")
        }

    if (!favId.isNullOrBlank()) {
        setFooter(favId)
    }

    this
}
