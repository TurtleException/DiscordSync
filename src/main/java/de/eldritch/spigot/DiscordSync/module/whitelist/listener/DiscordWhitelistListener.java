package de.eldritch.spigot.DiscordSync.module.whitelist.listener;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.module.whitelist.Request;
import de.eldritch.spigot.DiscordSync.module.whitelist.WhitelistModule;
import de.eldritch.spigot.DiscordSync.util.DiscordUtil;
import de.eldritch.spigot.DiscordSync.util.VerificationUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DiscordWhitelistListener extends ListenerAdapter {
    private final WhitelistModule module;

    public DiscordWhitelistListener(WhitelistModule module) {
        this.module = module;
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.isFromGuild()
                && DiscordSync.singleton.getDiscordAPI() != null
                && DiscordSync.singleton.getDiscordAPI().getGuild() != null
                && event.getGuild() != null
                && event.getGuild().equals(DiscordSync.singleton.getDiscordAPI().getGuild())
                && event.getTextChannel().equals(module.getChannel())
        ) {
            event.deferReply(true).queue();

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setFooter(DiscordUtil.FOOTER_TEXT, DiscordUtil.getAvatarURL())
                    .setTimestamp(DiscordUtil.getTimestamp())
                    .setColor(DiscordUtil.COLOR_NEUTRAL);

            // verify command
            if (!event.getName().equalsIgnoreCase("whitelist") || event.getMember() == null) {
                event.replyEmbeds(embedBuilder.setDescription("Etwas ist schief gelaufen... Wende dich bitte an die Serverleitung.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            OptionMapping nameOption = event.getOption("name");
            if (nameOption == null) {
                event.replyEmbeds(embedBuilder.setDescription("Bitte gib einen Namen an.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            // verify name
            UUID uuid = VerificationUtil.retrieveUUID(nameOption.getAsString());
            if (uuid == null) {
                event.replyEmbeds(embedBuilder.setDescription("Bitte gib einen gültigen Minecraft-Namen an.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            // create message
            event.replyEmbeds(embedBuilder
                    .setTitle(":pencil: Whitelist Anfrage")
                    .addField("Discord", event.getMember().getAsMention(), true)
                    .addField("Minecraft", nameOption.getAsString(), true)
                    .addField("Discord-ID", "`" + event.getMember().getId() + "`", false)
                    .addField("Minecraft UUID", "`" + uuid + "`", false)
                    .setThumbnail("https://mc-heads.net/body/" + uuid)
                    .build()
            ).setEphemeral(false).addActionRow(
                    Button.success("accept", "Annehmen"),
                    Button.danger("deny", "Ablehnen")
            ).queue();

            // create request
            module.request(Request.of(event.getMember(), uuid));
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.isFromGuild()
                && DiscordSync.singleton.getDiscordAPI() != null
                && DiscordSync.singleton.getDiscordAPI().getGuild() != null
                && event.getGuild() != null
                && event.getGuild().equals(DiscordSync.singleton.getDiscordAPI().getGuild())
                && event.getTextChannel().equals(module.getChannel())
                && event.getUser().equals(DiscordSync.singleton.getDiscordAPI().getJDA().getSelfUser())
                && event.getButton() != null
                && event.getButton().getId() != null
        ) {
            if (event.getButton().getId().equals("accept")) {
                event.getChannel().sendMessage("Accepted.").queue();
                // TODO
            } else if (event.getButton().getId().equals("deny")) {
                event.getChannel().sendMessage("Denied.").queue();
                // TODO
            }
        }
    }
}
