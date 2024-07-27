package de.turtleboi.spigot.dsync.api.chat;

import de.turtleboi.spigot.dsync.api.entity.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRouter {
    private final Set<MessageHandler> handlers = ConcurrentHashMap.newKeySet();
    private final MessageHistory history;

    public MessageRouter(int backlog) {
        this.history = new MessageHistory(backlog);
    }

    public void registerHandler(@NotNull MessageHandler handler) {
        this.handlers.add(handler);
    }

    public void unregisterHandler(@NotNull MessageHandler handler) {
        this.handlers.remove(handler);
    }

    public synchronized void handle(@NotNull Message message) {
        this.history.add(message);
        this.handlers.forEach(handler -> handler.onMessage(message));
    }
}
