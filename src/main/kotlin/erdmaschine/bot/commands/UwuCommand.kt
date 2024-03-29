package erdmaschine.bot.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.util.*
import kotlin.random.Random

private const val OPTION_TEXT = "text"
private const val OPTION_SPONGE = "sponge"
private const val stutterChance = 20
private const val emojiChance = 50

val UwuCommand = Commands.slash("uwu", "uwuwify a twext swipt uwu^^")
    .addOption(OptionType.STRING, OPTION_TEXT, "Text to be used", true)
    .addOption(
        OptionType.BOOLEAN,
        OPTION_SPONGE,
        "Spongeifiy as well"
    )

fun executeUwuCommand(event: SlashCommandInteractionEvent) {
    var text = event.getOption(OPTION_TEXT)?.asString ?: throw Exception("Text must be provided")

    if (event.getOption(OPTION_SPONGE)?.asBoolean == true) {
        text = text.spongeify()
    }

    event.reply(text.uwuify()).submit()
}

fun String.uwuify(): String {
    var output = this

    // replace some words
    var find = output.findAnyOf(words.keys, ignoreCase = true)
    while (find != null) {
        // because find.second is always lowercase
        val word = output.substring(find.first, find.first + find.second.length)

        var replace = words[find.second]!!

        // all caps
        if (!word.toCharArray().any { it.isLowerCase() }) {
            replace = replace.uppercase()
        }
        // first char is uppercase
        else if (word[0].isUpperCase()) {
            // capitalize
            replace = replace.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else {
                    it.toString()
                }
            }
        }

        output = output.replace(word, replace)

        find = output.findAnyOf(words.keys, ignoreCase = true)
    }

    // nya-ify
    output = output.replace("na", "nya")
    output = output.replace("Na", "Nya")
    output = output.replace("nA", "nyA")
    output = output.replace("NA", "NYA")

    // replace l and r with w
    output = output.replace('l', 'w')
    output = output.replace('r', 'w')
    output = output.replace('L', 'W')
    output = output.replace('R', 'W')

    // stutter sometimes
    var offset = 0
    for (s in output.split(" ")) {
        if (s.length > 1 && randomWithChance(stutterChance)) {
            output = output.prefixWord(s, "${s[0]}-", offset)
        }
        offset += s.length
    }

    // add a text emoji at the end sometimes
    if (!punctuation.contains(output.lastOrNull()) && randomWithChance(emojiChance)) {
        output += emojis[Random.nextInt(0, emojis.size - 1)]
    }

    // add a text emoji after punctuation sometimes
    val array = output.toCharArray()
    for ((eOffset, char) in array.withIndex()) {
        val index = array.indexOf(char)
        // ', ' or '! ' etc or last character of the input because I don't want emojis in text.like.this
        if (punctuation.contains(char)
            && (index == array.size - 1 || array[index + 1] == ' ')
            && randomWithChance(emojiChance)
        ) {
            output = output.suffixChar(char, emojis[Random.nextInt(0, emojis.size - 1)], eOffset)
        }
    }

    return output
}

private fun String.prefixWord(word: String, prefix: String, startIndex: Int = 0) =
    substring(0, indexOf(word, startIndex)) + prefix + substring(indexOf(word, startIndex))

private fun String.suffixChar(char: Char, suffix: String, startIndex: Int = 0) =
    substring(0, indexOf(char, startIndex) + 1) + suffix + substring(indexOf(char, startIndex) + 1)

private fun randomWithChance(chance: Int): Boolean = Random.nextInt(1, 101) <= chance

private val words = mapOf(
    Pair("small", "smol"),
    Pair("cute", "kawaii~"),
    Pair("fluff", "floof"),
    Pair("love", "luv"),
    Pair("stupid", "baka"),
    Pair("what", "nani"),
    Pair("meow", "nya~"),
)

private val emojis = listOf(
    " rawr x3",
    " OwO",
    " UwU",
    " o.O",
    " -.-",
    " >w<",
    " (⑅˘꒳˘)",
    " (ꈍᴗꈍ)",
    " (˘ω˘)",
    " (U ᵕ U❁)",
    " σωσ",
    " òωó",
    " (U ﹏ U)",
    " ʘwʘ",
    " :3",
    " XD",
    " nyaa~~",
    " mya",
    " >_<",
    " rawr",
    " ^^",
    " (^•ω•^)",
    " (✿oωo)",
    " („ᵕᴗᵕ„)",
    " (。U⁄ ⁄ω⁄ ⁄ U。)"
)

private val punctuation = listOf(',', '.', '!', '?')
