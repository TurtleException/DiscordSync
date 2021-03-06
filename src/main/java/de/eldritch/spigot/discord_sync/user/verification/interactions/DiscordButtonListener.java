package de.eldritch.spigot.discord_sync.user.verification.interactions;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.text.Text;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class DiscordButtonListener extends ListenerAdapter {
    public static final String BUTTON_ID_BLOCK_PLAYER = "verify.blockPlayer";
    public static final String BUTTON_ID_BLOCK_ALL    = "verify.blockAll";

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final String buttonId = event.getButton().getId();

        if (buttonId == null) return;
        if (!(buttonId.equals(BUTTON_ID_BLOCK_PLAYER) || buttonId.equals(BUTTON_ID_BLOCK_ALL))) return;

        event.deferReply().queue();

        try {
            DiscordSync.singleton.getUserService().addBlock(
                    "discord." + event.getUser().getId(),
                    buttonId.equals(BUTTON_ID_BLOCK_PLAYER)
                            ? this.handlePlayer(event)
                            : "*"
            );

            event.getHook().sendMessage(Text.of(buttonId.equals(BUTTON_ID_BLOCK_ALL)
                    ? "verify.blocked.blockAll"
                    : "verify.blocked.blockPlayer"
            ).content()).queue();
        } catch (Exception e) {
            DiscordSync.singleton.getLogger().log(Level.FINE, "An unexpected exception occurred when attempting to handle blocking interaction.", e);
            event.getHook().sendMessage(Text.of("error.internal").content()).queue();
        }
    }

    private String handlePlayer(ButtonInteractionEvent event) {
        final Optional<MessageEmbed.Field> field = event.getMessage()
                .getEmbeds().get(0)
                .getFields().stream()
                .filter(field1 -> "UUID".equals(field1.getName()))
                .findFirst();

        if (field.isEmpty())
            throw new NullPointerException("Name may not be null.");

        final String str = field.get().getValue();

        if (str == null)
            throw new NullPointerException("UUID may not be null.");

        return UUID.fromString(str).toString();
    }
}
