package de.eldritch.spigot.discord_sync.user.listener;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.Accessor;
import de.eldritch.spigot.discord_sync.user.User;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordNameListener extends ListenerAdapter {
    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        if (event.getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;

        final Accessor discordAccessor = DiscordSync.singleton.getDiscordService().getAccessor();

        final Member member = discordAccessor.getGuild().getMember(event.getUser());
        if (member == null) return;

        final User user = DiscordSync.singleton.getUserService().getBySnowflake(member.getIdLong());
        if (user == null) return;

        /* ----- ^^ GUARDS ^^ ----- */

        user.setName(event.getNewName());
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;

        final User user = DiscordSync.singleton.getUserService().getBySnowflake(event.getMember().getIdLong());
        if (user == null) return;

        /* ----- ^^ GUARDS ^^ ----- */

        final String name = event.getNewNickname() == null
                ? event.getMember().getEffectiveName()
                : event.getNewNickname();

        user.setName(name);
    }
}
