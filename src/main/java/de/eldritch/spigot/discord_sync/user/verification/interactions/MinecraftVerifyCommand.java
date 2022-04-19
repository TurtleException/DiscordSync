package de.eldritch.spigot.discord_sync.user.verification.interactions;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.discord.DiscordUtil;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.AvatarHandler;
import de.eldritch.spigot.discord_sync.user.User;
import de.eldritch.spigot.discord_sync.user.verification.VerificationUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinecraftVerifyCommand implements CommandExecutor {
    private static final String HELP_URL = ""; // TODO

    private static final int COLOR_ACCEPT = 0x2D7D46;
    private static final int COLOR_DENY   = 0xD83C3E;

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


        // check if the user has blocked requests
        final String memberKey = member.getId();
        final String   userKey = player.getUniqueId().toString();

        if (DiscordSync.singleton.getUserService().isBlocked(memberKey, userKey, true)) {
            player.spigot().sendMessage(
                    DiscordSync.getChatPrefix(),
                    Text.of("verify.error.blocked", member.getUser().getAsTag()).toBaseComponent()
            );
            return true;
        }


        // check if the old connection will be overwritten
        final User user = DiscordSync.singleton.getUserService().getByUUID(player.getUniqueId());
        if (user.discord() != null) {
            confirmInteraction(player, member, user.discord());
        } else {
            openInteraction(player, member);
        }

        return true;
    }

    private void confirmInteraction(@NotNull Player player, @NotNull Member member, @NotNull Member oldMember) {
        final TextComponent message = Text.of("verify.confirm.message", oldMember.getUser().getAsTag()).toBaseComponent();
        final TextComponent button  = Text.of("verify.confirm.button").toBaseComponent();

        MinecraftVerifyConfirmCommand.CONFIRMATION_QUEUE.put(player.getUniqueId(), member);
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/verify-confirm"));

        player.spigot().sendMessage(
                DiscordSync.getChatPrefix(),
                message,
                button
        );
    }

    static void openInteraction(@NotNull Player player, @NotNull Member member) {
        DiscordSync.singleton.getServer().getScheduler().runTaskAsynchronously(
                DiscordSync.singleton,
                () -> openBlockingDiscordInteraction(player, member)
        );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void openBlockingDiscordInteraction(@NotNull Player player, @NotNull Member member) {
        final User user = DiscordSync.singleton.getUserService().getByUUID(player.getUniqueId());

        final PrivateChannel channel = member.getUser().openPrivateChannel().complete();

        // interaction timeout
        final long timeout = Instant.now().plus(10L, ChronoUnit.MINUTES).toEpochMilli();

        final String textAccept      = Text.of("verify.discord.button.accept").content();
        final String textDeny        = Text.of("verify.discord.button.deny"  ).content();
        final String textBlockPlayer = Text.of("verify.discord.button.blockPlayer").content();
        final String textBlockAll    = Text.of("verify.discord.button.blockAll"   ).content();

        final Button buttonAccept      = Button.of(ButtonStyle.SUCCESS  , "accept", textAccept);
        final Button buttonDeny        = Button.of(ButtonStyle.DANGER   , "deny"  , textDeny  );
        final Button buttonBlockPlayer = Button.of(ButtonStyle.SECONDARY, DiscordButtonListener.BUTTON_ID_BLOCK_PLAYER, textBlockPlayer);
        final Button buttonBlockAll    = Button.of(ButtonStyle.SECONDARY, DiscordButtonListener.BUTTON_ID_BLOCK_ALL   , textBlockAll   );

        final EmbedBuilder embedBuilder = new EmbedBuilder(DiscordUtil.DEFAULT_EMBED)
                .setDescription(Text.of("verify.discord.embed.description", HELP_URL).content())
                .setTitle(Text.of("verify.discord.embed.title").content())
                .setThumbnail(AvatarHandler.getBodyURL(player))
                .addField(
                        Text.of("verify.discord.embed.field.player").content(),
                        player.getName(),
                        true
                )
                .addField(
                        Text.of("verify.discord.embed.field.guild").content(),
                        member.getGuild().getName() + " (" + member.getAsMention() + ")",
                        true
                )
                .addField(
                        "UUID",
                        player.getUniqueId().toString(),
                        false
                )
                .addField(
                        Text.of("verify.discord.embed.field.timeout").content(),
                        TimeFormat.RELATIVE.format(timeout),
                        false
                );

        final MessageBuilder builder = new MessageBuilder()
                .setActionRows(ActionRow.of(buttonAccept, buttonDeny, buttonBlockPlayer, buttonBlockAll))
                .setEmbeds(embedBuilder.build());

        final Message message = channel.sendMessage(builder.build()).complete();

        // notify player
        player.spigot().sendMessage(
                DiscordSync.getChatPrefix(),
                Text.of("verify.message", member.getUser().getAsTag()).toBaseComponent()
        );

        // interaction result
        final AtomicBoolean[] result = {
                // button clicked
                new AtomicBoolean(false),
                // handled by internal listener
                new AtomicBoolean(false),
                // request accepted
                new AtomicBoolean(false)
        };

        // Listener to handle button interactions
        final ListenerAdapter buttonListener = new ListenerAdapter() {
            @Override
            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                if (!event.getMessageId().equals(message.getId())) return;

                final String buttonId = event.getButton().getId();

                if (buttonId != null) {
                    if (buttonId.equals(buttonAccept.getId())) {
                        // prevent "Interaction failed"
                        event.deferEdit().queue();

                        result[1].set(true);
                        result[2].set(true);
                    } else if (buttonId.equals(buttonDeny.getId())) {
                        // prevent "Interaction failed"
                        event.deferEdit().queue();

                        result[1].set(true);
                        result[2].set(false);
                    }
                }

                // the "block X" buttons are not handled by temporary listeners

                // break loop
                result[0].set(true);
            }
        };

        // register listener
        member.getJDA().getEventManager().register(buttonListener);

        // interaction timeout after 10 minutes
        while (System.currentTimeMillis() < timeout && !result[0].get()) { }

        if (result[0].get() && result[1].get())
            embedBuilder.setColor(result[2].get()
                    ? COLOR_ACCEPT
                    : COLOR_DENY
            );
        else if (result[0].get())
            // notify player about blocking interaction
            embedBuilder.setColor(
                    Color.BLACK
            );

        message.editMessage(builder
                .setEmbeds(embedBuilder.build())
                .setActionRows(ActionRow.of(
                        buttonAccept.asDisabled(),
                        buttonDeny.asDisabled(),
                        // disable block buttons if the player accepted the request
                        result[1].get() && result[2].get() ? buttonBlockPlayer : buttonBlockPlayer.asDisabled(),
                        result[1].get() && result[2].get() ? buttonBlockAll : buttonBlockAll.asDisabled()
                ))
                .build()
        ).complete();

        // unregister listener
        member.getJDA().getEventManager().unregister(buttonListener);

        // TODO: is it safe to use the Spigot API async here?

        // notify player
        if (!result[0].get()) {
            player.spigot().sendMessage(
                    DiscordSync.getChatPrefix(),
                    Text.of("verify.error.timedOut", member.getUser().getAsTag()).toBaseComponent()
            );
        } else {
            if (result[1].get() && result[2].get()) {
                // merge user object
                user.setMember(member);

                player.spigot().sendMessage(
                        DiscordSync.getChatPrefix(),
                        Text.of("verify.success", member.getUser().getAsTag()).toBaseComponent()
                );
            } else {
                player.spigot().sendMessage(
                        DiscordSync.getChatPrefix(),
                        Text.of("verify.error.denied", member.getUser().getAsTag()).toBaseComponent()
                );
            }
        }
    }
}
