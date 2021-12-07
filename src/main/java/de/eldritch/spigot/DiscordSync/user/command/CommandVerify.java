package de.eldritch.spigot.DiscordSync.user.command;

import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.user.User;
import de.eldritch.spigot.DiscordSync.util.DiscordUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.Button;
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

public class CommandVerify implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("verify")) return false;
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.spigot().sendMessage(DiscordSync.singleton.getMessageService().get("general.prefix"),
                    DiscordSync.singleton.getMessageService().get("user.verify.missingName"),
                    DiscordSync.singleton.getMessageService().get("user.verify.example")
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
            player.spigot().sendMessage(
                    DiscordSync.singleton.getMessageService().get("general.prefix"),
                    DiscordSync.singleton.getMessageService().get("misc.internalError")
            );
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

        player.spigot().sendMessage(
                DiscordSync.singleton.getMessageService().get("general.prefix"),
                DiscordSync.singleton.getMessageService().get("user.verify.discordUserNotFound", name.toString()),
                DiscordSync.singleton.getMessageService().get("user.verify.example")
        );
        return true;
    }

    private void onMatch(Player player, Member member) {
        User checkUser = DiscordSync.singleton.getUserAssociationService().get(user -> user.getDiscord().getId().equals(member.getId()));
        if (checkUser != null) {
            player.spigot().sendMessage(
                    DiscordSync.singleton.getMessageService().get("general.prefix"),
                    DiscordSync.singleton.getMessageService().get("user.verify.discordUserAlreadyRegistered", member.getEffectiveName())
            );
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

                player.spigot().sendMessage(
                        DiscordSync.singleton.getMessageService().get("general.prefix"),
                        DiscordSync.singleton.getMessageService().get("user.verify.requestSuccess")
                );


            }, throwable -> {

                /* --- FAILED TO SEND MESSAGE --- */

                player.spigot().sendMessage(
                        DiscordSync.singleton.getMessageService().get("general.prefix"),
                        DiscordSync.singleton.getMessageService().get("user.verify.error.general"),
                        DiscordSync.singleton.getMessageService().get("user.verify.error.sendMessage", member.getEffectiveName())
                );


            });
        }, throwable -> {

            /* --- FAILED TO OPEN CHANNEL --- */

            player.spigot().sendMessage(
                    DiscordSync.singleton.getMessageService().get("general.prefix"),
                    DiscordSync.singleton.getMessageService().get("user.verify.error.general"),
                    DiscordSync.singleton.getMessageService().get("user.verify.error.openChannel", member.getEffectiveName())
            );


        });
    }
}
