package de.eldritch.spigot.DiscordSync.user.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordNameListener extends ListenerAdapter {
    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        DiscordSync.singleton.getUserAssociationService().update(event.getMember(), event.getNewNickname());
        DiscordSync.singleton.getLogger().info("User " + event.getMember().getId() + " has been renamed to '" + event.getNewNickname() + "'.");
    }

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        if (DiscordSync.singleton.getDiscordAPI().getGuild() != null) {
            Member member = DiscordSync.singleton.getDiscordAPI().getGuild().getMember(event.getUser());
            if (member != null && member.getNickname() == null) {
                DiscordSync.singleton.getUserAssociationService().update(member, member.getEffectiveName());
                DiscordSync.singleton.getLogger().info("User " + event.getUser().getId() + " has been renamed to '" + event.getNewName() + "'.");
            }
        }
    }
}