package de.eldritch.spigot.DiscordSync.module.whitelist;

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// TODO
public class Request {
    private Member member;
    private UUID uuid;

    private Status status;

    private enum Status {
        PENDING, ACCEPTED, DENIED
    }

    private Request(Member member, UUID uuid) {
        this.member = member;
        this.uuid = uuid;

        this.update();
    }

    public static @NotNull Request of(Member member, UUID uuid) {
        return new Request(member, uuid);
    }

    public void update() {

    }
}
