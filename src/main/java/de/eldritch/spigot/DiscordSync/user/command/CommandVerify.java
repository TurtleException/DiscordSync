package de.eldritch.spigot.DiscordSync.user.command;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.eldritch.spigot.DiscordSync.DiscordSync;
import de.eldritch.spigot.DiscordSync.message.Container;
import de.eldritch.spigot.DiscordSync.message.MessageService;
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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CommandVerify implements CommandExecutor {
    private Multimap<UUID, Long> requestCache = HashMultimap.create();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("verify")) return false;
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            MessageService.sendMessage(player,
                    "user.verify.missingName",
                    "user.verify.example"
            );
            return true;
        }

        // check if player is already registered
        User user = DiscordSync.singleton.getUserAssociationService().get(user1 -> user1.getMinecraft().getUniqueId().equals(player.getUniqueId()));
        if (user != null) {
            MessageService.sendMessage(player,
                    Container.of("user.verify.error.alreadyRegistered.minecraft", DiscordUtil.getActualName(user.getDiscord().getUser()))
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
            MessageService.sendMessage(player,
                    "misc.internalError"
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

        // match effective name (basically nickname from a users' perspective)
        for (Member member : members) {
            if (member.getEffectiveName().equals(name.toString())) {
                this.onMatch(player, member);
                return true;
            }
        }

        MessageService.sendMessage(player,
                Container.of("user.verify.error.notFound", name.toString()),
                "user.verify.example"
        );
        return true;
    }

    private void onMatch(Player player, Member member) {
        // check if user is already registered
        User checkUser = DiscordSync.singleton.getUserAssociationService().get(user -> user.getDiscord().getId().equals(member.getId()));
        if (checkUser != null) {
            MessageService.sendMessage(player,
                    Container.of("user.verify.error.alreadyRegistered.discord", DiscordUtil.getActualName(member.getUser()))
            );
            return;
        }

        // check if user has blocked requests from player
        List<String> blocks = DiscordSync.singleton.getUserAssociationService().getBlockedSection().getStringList(member.getId());
        if (blocks.contains(player.getUniqueId().toString()) || blocks.contains("*")) {
            MessageService.sendMessage(player,
                    Container.of("user.verify.error.blocked", DiscordUtil.getActualName(member.getUser()))
            );
            return;
        }

        member.getUser().openPrivateChannel().queue(privateChannel -> {

            // check if request is already queued
            if (requestCache.containsEntry(player.getUniqueId(), member.getIdLong())) {
                MessageService.sendMessage(player,
                        Container.of("user.verify.error.alreadyRequested", DiscordUtil.getActualName(member.getUser()))
                );
                return;
            }

            privateChannel.sendMessageEmbeds(new EmbedBuilder(DiscordUtil.getDefaultEmbed())
                    .setTitle("Minecraft Account-Verknüpfung")
                    .setDescription("""
                            Hey, bist du das?
                            Mit einem Klick auf `Ja` verknüpfst du diesen Discord-Account mit dem Minecraft-Konto, um bspw. deinen Namen zu synchronisieren. Das gilt natürlich nur für den jeweiligen Discord-Server. Die Anfrage läuft in 10 Minuten ab.
                            Um in Zukunft keine Anfragen dieses Spielers/dieser Spielerin zu erhalten kannst du den Account oder direkt alle Anfragen dauerhaft blockieren.""")
                    .setThumbnail("https://mc-heads.net/body/" + player.getUniqueId())
                    .addField("Spieler*in", player.getName(), true)
                    .addField("Server", member.getGuild().getName() + (
                            member.getGuild().getOwner() != null ? (" (" + member.getGuild().getOwner().getAsMention() + ")") : null
                            ), true)
                    .build()
            ).setActionRow(
                    Button.success("accept", "Ja"),
                    Button.danger("deny", "Nein"),
                    Button.secondary("block", "Blockieren"),
                    Button.secondary("block-all", "Alle Anfragen blockieren")
            ).queue(message -> {

                /* --- MESSAGE SENT SUCCESSFULLY --- */

                DiscordSync.singleton.getUserAssociationService().getRequestConfig().set(message.getId(), player.getUniqueId());

                // disable all buttons except "block" after 10 minutes
                ArrayList<Button> buttons = new ArrayList<>(message.getButtons());
                for (int i = 0; i < buttons.size(); i++) {
                    if (!Objects.equals(buttons.get(i).getId(), "block") && !Objects.equals(buttons.get(i).getId(), "block-all")) {
                        buttons.set(i, buttons.get(i).asDisabled());
                    }
                }
                message.editMessage(message).setActionRow(buttons.stream().toList()).queueAfter(10L, TimeUnit.MINUTES, message1 -> {
                    DiscordSync.singleton.getUserAssociationService().getRequestConfig().set(message.getId(), null);
                });


                MessageService.sendMessage(player,
                        "user.verify.requestSuccess"
                );


            }, throwable -> {

                /* --- FAILED TO SEND MESSAGE --- */

                MessageService.sendMessage(player,
                        "user.verify.error.general",
                        Container.of("user.verify.error.sendMessage", DiscordUtil.getActualName(member.getUser()))
                );


            });
        }, throwable -> {

            /* --- FAILED TO OPEN CHANNEL --- */

            MessageService.sendMessage(player,
                    "user.verify.error.general",
                    Container.of("user.verify.error.openChannel", DiscordUtil.getActualName(member.getUser()))
            );


        });
    }
}
