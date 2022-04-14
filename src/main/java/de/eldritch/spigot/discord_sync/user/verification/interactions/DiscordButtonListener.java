package de.eldritch.spigot.discord_sync.user.verification.interactions;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordButtonListener extends ListenerAdapter {
    public static final String BUTTON_ID_BLOCK_PLAYER = "verify.blockPlayer";
    public static final String BUTTON_ID_BLOCK_ALL    = "verify.blockAll";

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final String buttonId = event.getButton().getId();

        if (buttonId == null) return;
        if (!(buttonId.equals(BUTTON_ID_BLOCK_PLAYER) || buttonId.equals(BUTTON_ID_BLOCK_ALL))) return;

        // TODO: handle block interaction


    }
}
