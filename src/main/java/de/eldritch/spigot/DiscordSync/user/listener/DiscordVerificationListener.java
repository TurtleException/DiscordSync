package de.eldritch.spigot.DiscordSync.user.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.Container;
import de.eldritch.spigot.DiscordSync.message.MessageService;
import de.eldritch.spigot.DiscordSync.user.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class DiscordVerificationListener extends ListenerAdapter {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.isFromGuild()
                || DiscordSync.singleton.getDiscordAPI() == null
                || DiscordSync.singleton.getDiscordAPI().getGuild() == null
                || !event.getMessage().getAuthor().equals(DiscordSync.singleton.getDiscordAPI().getJDA().getSelfUser())
                || event.getButton() == null
                || event.getButton().getId() == null
                || !this.isVerificationMessage(event.getMessage())
                || DiscordSync.singleton.getDiscordAPI().getGuild().getMember(event.getUser()) == null
        ) return;

        MessageEmbed embed = event.getMessage().getEmbeds().get(0);
        String uuidStr = DiscordSync.singleton.getUserAssociationService().getRequestConfig().getString(event.getMessageId());
        if (uuidStr == null || embed == null) {
            event.reply("Ein interner Fehler ist aufgetreten.").setEphemeral(true).queue();
            return;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            event.reply("Ein interner Fehler ist aufgetreten.").setEphemeral(true).queue();
            return;
        }

        Member member = DiscordSync.singleton.getDiscordAPI().getGuild().getMemberById(event.getUser().getId());
        if (member == null) {
            event.reply("Ein interner Fehler ist aufgetreten.").setEphemeral(true).queue();
            return;
        }

        // mark the interaction as successful
        event.deferEdit().queue();

        // retrieve player object, if the player is online
        Player player = DiscordSync.singleton.getServer().getPlayer(uuid);

        if (event.getButton().getId().equals("accept")) {
            DiscordSync.singleton.getUserAssociationService().registerUser(new User(
                    DiscordSync.singleton.getServer().getPlayer(uuid), member
            ));
            if (player != null && player.isOnline()) {
                MessageService.sendMessage(player, "user.verify.requestAccepted");
            }
            event.getMessage().editMessageEmbeds(new EmbedBuilder(embed).setColor(Color.GREEN).build()).queue();
        } else if (event.getButton().getId().equals("deny")) {
            if (player != null && player.isOnline()) {
                MessageService.sendMessage(player,
                        Container.of("user.verify.requestDenied", event.getUser().getName() + "#" + event.getUser().getDiscriminator())
                );
            }
            event.getMessage().editMessageEmbeds(new EmbedBuilder(embed).setColor(Color.RED).build()).queue();
        } else if (event.getButton().getId().equals("block")) {
            List<String> userBlocks = DiscordSync.singleton.getUserAssociationService().getBlockedSection().getStringList(event.getUser().getId());
            userBlocks.add(uuid.toString());
            DiscordSync.singleton.getUserAssociationService().getBlockedSection().set(event.getUser().getId(), userBlocks);
        } else if (event.getButton().getId().equals("block-all")) {
            DiscordSync.singleton.getUserAssociationService().getBlockedSection().set(event.getUser().getId(), List.of("*"));
        }

        // delete request
        DiscordSync.singleton.getUserAssociationService().getRequestConfig().set(event.getMessageId(), null);

        // disable buttons
        event.getHook().editOriginalComponents(ActionRow.of(event.getMessage().getButtons().stream().map(Button::asDisabled).toList())).queue();
    }

    public boolean isVerificationMessage(Message message) {
        return DiscordSync.singleton.getUserAssociationService().getRequestConfig().contains(message.getId());
    }
}
