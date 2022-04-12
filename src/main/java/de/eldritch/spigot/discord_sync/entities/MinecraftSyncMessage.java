package de.eldritch.spigot.discord_sync.entities;

import de.eldritch.spigot.discord_sync.DiscordSync;
import de.eldritch.spigot.discord_sync.entities.interfaces.MinecraftSynchronizable;
import de.eldritch.spigot.discord_sync.text.Text;
import de.eldritch.spigot.discord_sync.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class MinecraftSyncMessage extends Message implements MinecraftSynchronizable {
    protected MinecraftSyncMessage(long turtle, @NotNull User author, long timestamp, String content, @Nullable Message reference) {
        super(turtle, author, timestamp, content, reference);
    }

    protected void sendToMinecraft(@NotNull String key) {
        final TextComponent prefix = Text.of("chat.message." + key, author.getName()).toBaseComponent();
        final TextComponent text   = Text.of("chat.message.content", getFormat()).toBaseComponent();

        text.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.hover.content.Text(Text.of("chat.reference.howto").content())
        ));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + getRefNum() + " "));

        if (reference == null) {
            DiscordSync.singleton.getServer().spigot().broadcast(prefix, text);
        } else {
            TextComponent reply = Text.of("chat.message.reply").toBaseComponent();

            reply.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.hover.content.Text(reference.getContainerText().content())));

            DiscordSync.singleton.getServer().spigot().broadcast(prefix, reply, text);
        }
    }
}
