package de.eldritch.spigot.DiscordSync.module.name.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.name.NameModule;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordNameListener extends ListenerAdapter {
    private final NameModule module;

    public DiscordNameListener(NameModule module) {
        this.module = module;
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        module.set(event.getMember().getIdLong(), event.getNewNickname() != null ? event.getNewNickname() : event.getMember().getEffectiveName());
    }

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        Member member = DiscordSync.singleton.getDiscordAPI().getGuild().getMember(event.getUser());
        if (member != null) {
            // only change the synchronized name if the member does not have a nickname
            if (member.getNickname() == null) {
                module.set(event.getUser().getIdLong(), event.getNewName());
            }
        }
    }
}
