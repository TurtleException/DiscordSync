package de.eldritch.spigot.DiscordSync.module.whitelist;

import de.eldritch.spigot.DiscordSync.util.VerificationUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    // TODO: remove
    public static @Nullable Request from(Message message) {
        UUID uuid = VerificationUtil.retrieveUUID(message.getContentStripped());

        if (uuid != null) {
            return new Request(message.getMember(), uuid);
        } else {
            return null;
        }
    }

    public static @NotNull Request of(Member member, UUID uuid) {
        return new Request(member, uuid);
    }

    public void update() {

    }
}
