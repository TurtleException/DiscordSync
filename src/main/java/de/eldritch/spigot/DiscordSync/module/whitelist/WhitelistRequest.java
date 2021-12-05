package de.eldritch.spigot.DiscordSync.module.whitelist;

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// TODO
public class WhitelistRequest {
    private Member member;
    private UUID uuid;

    private Status status;

    private enum Status {
        PENDING, ACCEPTED, DENIED
    }

    private WhitelistRequest(Member member, UUID uuid) {
        this.member = member;
        this.uuid = uuid;

        this.update();
    }

    public static @NotNull WhitelistRequest of(Member member, UUID uuid) {
        return new WhitelistRequest(member, uuid);
    }

    public void update() {

    }

    public Member getMember() {
        return member;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Status getStatus() {
        return status;
    }
}
