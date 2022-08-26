package erdmaschine.bot.commands

import erdmaschine.bot.model.Fallacies
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

val subCommands = Fallacies.map { (name, description) ->
    SubcommandData(name, description)
}

val FallacyCommand = Commands.slash("fallacy", "Post a fallacy referee image")
    .addSubcommands(subCommands)

fun executeFallacyCommand(event: SlashCommandInteractionEvent) {
    val filename = "${event.subcommandName}.jpg"
    val data = object {}.javaClass.getResource("/fallacies/$filename")
        ?: throw Exception("Unknown subcommand or image file not found")

    event.deferReply().submit()
    event.hook.sendFile(data.readBytes(), filename).submit()
}
