package de.eldritch.spigot.DiscordSync.user.command;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.util.DiscordUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.Button;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

// TODO: simplify
public class CommandVerify implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("verify")) return false;
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.spigot().sendMessage(DiscordSync.getChatPrefix(),
                    new TextComponent(ChatColor.GRAY + "Bitte gib deinen Discord Namen an! "),
                    this.getExamplesAsHover()
            );
            return true;
        }

        // build name
        StringBuilder name = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            name.append(" ").append(args[i]);
        }

        // check errors
        if (DiscordSync.singleton.getDiscordAPI() == null || DiscordSync.singleton.getDiscordAPI().getGuild() == null) {
            player.spigot().sendMessage(DiscordSync.getChatPrefix(), new TextComponent(ChatColor.GRAY + "Ein interner Fehler ist aufgetreten."));
            DiscordSync.singleton.getLogger().log(Level.WARNING, "Illegal state when calling verify command. Discord API is unavailable.");
            return true;
        }


        List<Member> members = DiscordSync.singleton.getDiscordAPI().getGuild().getMembers();

        // match id
        for (Member member : members) {
            if (member.getId().equals(name.toString())) {
                this.onMatch(player, member);
                return true;
            }
        }

        // match name + discriminator
        for (Member member : members) {
            if ((member.getUser().getName() + "#" + member.getUser().getDiscriminator()).equals(name.toString())) {
                this.onMatch(player, member);
                return true;
            }
        }

        // match nickname
        for (Member member : members) {
            if (member.getNickname() != null && member.getNickname().equals(name.toString())) {
                this.onMatch(player, member);
                return true;
            }
        }

        player.spigot().sendMessage(DiscordSync.getChatPrefix(),
                new TextComponent(ChatColor.GRAY + "Discord User "),
                new TextComponent(ChatColor.GOLD + name.toString()),
                new TextComponent(ChatColor.GRAY + " nicht gefunden. "),
                this.getExamplesAsHover()
        );
        return true;
    }

    private void onMatch(Player player, Member member) {
        User checkUser = DiscordSync.singleton.getUserAssociationService().get(user -> user.getDiscord().getId().equals(member.getId()));
        if (checkUser != null) {
            player.spigot().sendMessage(new ComponentBuilder(DiscordSync.getChatPrefix())
                    .append(member.getEffectiveName()).color(ChatColor.GOLD)
                    .append(" ist bereits registriert.").color(ChatColor.GRAY)
                    .create());
            return;
        }

        member.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(new EmbedBuilder(DiscordUtil.getDefaultEmbed())
                    .setTitle("Minecraft Account-Verknüpfung")
                    .setDescription("Es wird versucht einen Minecraft-Account mit diesem Discord-Account zu verbinden.\n"
                            + "Du hast 10 Minuten, um zu bestätigen/abzulehnen. Um in Zukunft keine Anfragen dieses Spielers"
                            + "/dieser Spielerin zu erhalten kannst du den dauerhaft Account blockieren.")
                    .setThumbnail("https://mc-heads.net/body/" + player.getUniqueId())
                    .addField("Spieler*in", player.getName(), true)
                    .addField("Server", member.getGuild().getName() + (
                            member.getGuild().getOwner() != null ? (" (" + member.getGuild().getOwner().getAsMention() + ")") : null
                            ), true)
                    .build()
            ).setActionRow(
                    Button.success("accept", "Ja"),
                    Button.danger("deny", "Nein"),
                    Button.secondary("block", "Blockieren")
            ).queue(message -> {

                /* --- MESSAGE SENT SUCCESSFULLY --- */

                // disable all buttons except "block" after 10 minutes
                ArrayList<Button> buttons = new ArrayList<>(message.getButtons());
                for (int i = 0; i < buttons.size(); i++) {
                    if (!Objects.equals(buttons.get(i).getId(), "block")) {
                        buttons.set(i, buttons.get(i).asDisabled());
                    }
                }
                message.editMessage(message).setActionRow(buttons.stream().toList()).queueAfter(10L, TimeUnit.MINUTES);

                player.spigot().sendMessage(DiscordSync.getChatPrefix(),
                        new TextComponent(ChatColor.GRAY + "Bitte schau in deine Discord Nachrichten.")
                );


            }, throwable -> {

                /* --- FAILED TO SEND MESSAGE --- */

                TextComponent hover = new TextComponent(new ComponentBuilder()
                        .append("Fehler").color(ChatColor.RED)
                        .append(": ").color(ChatColor.DARK_GRAY)
                        .append("Private Nachricht konnte nicht gesendet werden.\n\n")
                        .append("Um zu verifizieren, dass es sich bei ")
                        .append(member.getEffectiveName()).color(ChatColor.GOLD)
                        .append("tatsächlich um dich\n")
                        .append("handelt, schreibt dir der Bot eine Nachricht auf Discord, die du bestätigen\n")
                        .append("musst. Beim senden der Nachricht ist aber etwas schief gelaufen.\n\n")
                        .append("Mögliche Lösung").color(ChatColor.GREEN)
                        .append(": ").color(ChatColor.DARK_GRAY)
                        .append("Überprüfe, ob du Nachrichten von Servermitgliedern erhalten\n")
                        .append("kannst. Ist das nicht der Fall, können wir leider nicht verifizieren, dass\n")
                        .append("dir dieser Account tatsächlich gehört. Wende dich in diesem Fall bitte an\n")
                        .append("die Serverleitung.")
                        .create());
                hover.setColor(ChatColor.GRAY);

                player.spigot().sendMessage(DiscordSync.getChatPrefix(),
                        new TextComponent(new ComponentBuilder()
                                .append("Verifikation fehlgeschlagen. ").color(ChatColor.GRAY)
                                .create()),
                        new TextComponent(new ComponentBuilder(ChatColor.DARK_GRAY + "[Warum?]")
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{hover})))
                                .create())
                );


            });
        }, throwable -> {

            /* --- FAILED TO OPEN CHANNEL --- */

            TextComponent hover = new TextComponent(new ComponentBuilder()
                    .append("Fehler").color(ChatColor.RED)
                    .append(": ").color(ChatColor.DARK_GRAY)
                    .append("Privater Channel konnte nicht geöffnet werden.\n\n")
                    .append("Um zu verifizieren, dass es sich bei ")
                    .append(member.getEffectiveName()).color(ChatColor.GOLD)
                    .append("tatsächlich um dich\n")
                    .append("handelt, schreibt dir der Bot eine Nachricht auf Discord, die du bestätigen\n")
                    .append("musst. Beim öffnen des privaten Channels ist aber etwas schief gelaufen und\n")
                    .append("der Bot kann dir keine Nachricht schreiben.\n\n")
                    .append("Mögliche Lösung").color(ChatColor.GREEN)
                    .append(": ").color(ChatColor.DARK_GRAY)
                    .append("Überprüfe, ob du Nachrichten von Servermitgliedern erhalten\n")
                    .append("kannst. Ist das nicht der Fall, können wir leider nicht verifizieren, dass\n")
                    .append("dir dieser Account tatsächlich gehört. Wende dich in diesem Fall bitte an\n")
                    .append("die Serverleitung.")
                    .create());
            hover.setColor(ChatColor.GRAY);

            player.spigot().sendMessage(DiscordSync.getChatPrefix(),
                    new TextComponent(new ComponentBuilder()
                            .append("Verifikation fehlgeschlagen. ").color(ChatColor.GRAY)
                            .create()),
                    new TextComponent(new ComponentBuilder(ChatColor.DARK_GRAY + "[Warum?]")
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{hover})))
                            .create())
            );


        });
    }

    private TextComponent getExamplesAsHover() {
        return new TextComponent(new ComponentBuilder(ChatColor.DARK_GRAY + "[BEISPIELE]")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.getExamples())))
                .create());
    }

    private BaseComponent[] getExamples() {
        return new BaseComponent[]{new TextComponent(
                         ChatColor.GRAY + "Discord-Name" + ChatColor.DARK_GRAY + " (+Discriminator)"
                + "\n" + ChatColor.AQUA + "/verify TurtleException#8673"
                + "\n"
                + "\n" + ChatColor.GRAY + "Server-Nickname" + ChatColor.DARK_GRAY + " (beta)"
                + "\n" + ChatColor.AQUA + "/verify Turtleboi"
                + "\n"
                + "\n" + ChatColor.GRAY + "Discord ID"
                + "\n" + ChatColor.AQUA + "/verify 871569116564172820"
        )};
    }
}
