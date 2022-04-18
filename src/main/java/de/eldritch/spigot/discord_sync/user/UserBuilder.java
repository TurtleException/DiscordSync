package de.eldritch.spigot.discord_sync.user;

import net.dv8tion.jda.api.entities.Member;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserBuilder {
    private Long          turtle   = null;
    private UserService   uService = null;
    private OfflinePlayer player   = null;
    private Member        member   = null;
    private String        name     = null;

    public UserBuilder() { }

    /* ----- ----- ----- */

    public @NotNull User build() {
        try {
            if (turtle == null)
                throw new IllegalArgumentException("Turtle ID may not be null.");

            final User user = new User(turtle, uService, player, member, name);

            uService.register(user);

            return user;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not build User due to an exception.", e);
        }
    }

    /* ----- ----- ----- */

    public UserBuilder setTurtle(long turtle) {
        this.turtle = turtle;
        return this;
    }

    public UserBuilder setUserService(UserService uService) {
        this.uService = uService;
        return this;
    }

    public UserBuilder setPlayer(@Nullable OfflinePlayer player) {
        this.player = player;
        return this;
    }

    public UserBuilder setMember(@Nullable Member member) {
        this.member = member;
        return this;
    }

    public UserBuilder setName(@Nullable String name) {
        this.name = name;
        return this;
    }
}
