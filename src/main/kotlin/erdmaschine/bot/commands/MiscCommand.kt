package erdmaschine.bot.commands

import dev.minn.jda.ktx.interactions.commands.Subcommand
import dev.minn.jda.ktx.messages.EmbedBuilder
import erdmaschine.bot.replyError
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val SUBCOMMAND_ROLE = "rolereferences"
private const val OPTION_ROLE = "role"

private val roleReferences = Subcommand(SUBCOMMAND_ROLE, "shows all channels that reference a role")
    .addOption(OptionType.ROLE, OPTION_ROLE, "the role to check", true)

val MiscCommand = Commands.slash("misc", "execute various useful functions")
    .addSubcommands(roleReferences)

fun executeMiscCommand(event: SlashCommandInteractionEvent) {
    when (event.subcommandName) {
        SUBCOMMAND_ROLE -> executeRoleReferences(event)
    }
}

private fun executeRoleReferences(event: SlashCommandInteractionEvent) {
    event.deferReply(true).submit()

    if (false == event.member?.hasPermission(Permission.MANAGE_ROLES)) {
        throw Exception("You are not authorized to manage roles on this server")
    }

    val role = event.getOption(OPTION_ROLE)?.asRole ?: return event.hook.replyError("Role must be provided")
    val channels = event.guild?.getChannels(true)?.mapNotNull { channel ->
        when (channel.permissionContainer.rolePermissionOverrides.any { it.role == role }) {
            true -> "<#${channel.id}>"
            else -> null
        }
    }.orEmpty()

    val embed = EmbedBuilder()
        .also {
            it.title = "Role References"
            it.description = "Role: <@&${role.id}>"
        }
    embed.field("Channels", channels.take(25).joinToString(" "), inline = true)
    event.hook.editOriginalEmbeds(embed.build()).submit()
}
