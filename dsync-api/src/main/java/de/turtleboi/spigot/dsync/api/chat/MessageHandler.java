package de.turtleboi.spigot.dsync.api.chat;

import de.turtleboi.spigot.dsync.api.entity.Message;
import org.jetbrains.annotations.NotNull;

public interface MessageHandler {
    void onMessage(@NotNull Message message);
}
