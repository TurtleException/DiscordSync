package de.eldritch.spigot.discord_sync.user.verification.interactions;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.user.verification.VerificationUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class MinecraftVerifyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (args.length < 1) return false;


        final String input  = String.join(" ", args);
        final Member member = VerificationUtil.parseMember(input);

        // check if member could be parsed
        if (VerificationUtil.checkMember(member, player, input)) return true;
        // so IntelliJ doesn't complain -> member is NEVER null at this point
        assert member != null;

        DiscordSync.singleton.getServer().getScheduler().runTaskAsynchronously(
                DiscordSync.singleton,
                () -> openBlockingDiscordInteraction(player, member)
        );

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void openBlockingDiscordInteraction(@NotNull Player player, @NotNull Member member) {
        // TODO: maybe check if the old connection will be overwritten
        final User user = DiscordSync.singleton.getUserService().getUserByUUID(player.getUniqueId());

        final PrivateChannel channel = member.getUser().openPrivateChannel().complete();

        final Button buttonAccept      = Button.of(ButtonStyle.SUCCESS  , "accept", "ACCEPT");
        final Button buttonDeny        = Button.of(ButtonStyle.DANGER   , "deny"  , "DENY"  );
        final Button buttonBlockPlayer = Button.of(ButtonStyle.SECONDARY, DiscordButtonListener.BUTTON_ID_BLOCK_PLAYER, "BLOCK PLAYER");
        final Button buttonBlockAll    = Button.of(ButtonStyle.SECONDARY, DiscordButtonListener.BUTTON_ID_BLOCK_ALL   , "BLOCK ALL"   );

        // TODO: complete builder
        final MessageBuilder builder = new MessageBuilder()
                .setActionRows(ActionRow.of(buttonAccept, buttonDeny, buttonBlockPlayer, buttonBlockAll));

        final Message message = channel.sendMessage(builder.build()).complete();

        // interaction result: {button clicked, request accepted}
        final boolean[] result = {false, false};

        // Listener to handle button interactions
        final ListenerAdapter buttonListener = new ListenerAdapter() {
            @Override
            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                if (!event.getMessageId().equals(message.getId())) return;

                // prevent "Interaction failed"
                event.deferEdit().queue();

                final String buttonId = event.getId();
                if (buttonId.equals(buttonAccept.getId())) {
                    result[1] = true;
                } else if (buttonId.equals(buttonDeny.getId())) {
                    result[1] = false;
                }

                // the "block X" buttons are not handled by temporary listeners

                // break loop
                result[0] = true;
            }
        };

        // register listener
        member.getJDA().getEventManager().register(buttonListener);

        // interaction timeout after 10 minutes
        final long timeout = Instant.now().plus(10L, ChronoUnit.MINUTES).toEpochMilli();
        while (System.currentTimeMillis() < timeout && !result[0]) { }

        message.editMessage(builder
                .setActionRows(ActionRow.of(
                        buttonAccept.asDisabled(),
                        buttonDeny.asDisabled(),
                        buttonBlockPlayer,
                        buttonBlockAll
                ))
                .build()
        ).complete();

        // unregister listener
        member.getJDA().getEventManager().unregister(buttonListener);

        // TODO: is it safe to use the Spigot API async here?

        // notify player
        if (!result[0]) {
            // timed out
            player.spigot().sendMessage(
                    DiscordSync.getChatPrefix(),
                    Text.of("verify.error.timedOut", member.getUser().getAsTag()).toBaseComponent()
            );
        } else {
            if (result[1]) {
                // request accepted
                // TODO: update user object
                // TODO: possibly merge user objects

                player.spigot().sendMessage(
                        DiscordSync.getChatPrefix(),
                        Text.of("verify.success", member.getUser().getAsTag()).toBaseComponent()
                );
            } else {
                // request denied
                player.spigot().sendMessage(
                        DiscordSync.getChatPrefix(),
                        Text.of("verify.error.denied", member.getUser().getAsTag()).toBaseComponent()
                );
            }
        }
    }
}
