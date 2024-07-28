package de.turtleboi.spigot.dsync.api.chat;

import de.turtleboi.spigot.dsync.api.entity.Message;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MessageHistory implements Iterable<Message> {
    private static class Node {
        private final Message message;
        private Node older;
        private Node newer;

        public Node(Message message, Node older) {
            this.message = message;
            this.older = older;
        }
    }

    private final int capacity;
    private int size = 0;

    private Node oldest;
    private Node newest;

    public MessageHistory(int capacity) {
        this.capacity = capacity;
    }

    public int size() {
        return this.size;
    }

    public int capacity() {
        return this.capacity;
    }

    @Override
    public @NotNull Iterator<Message> iterator() {
        final Node[] current = { this.newest };

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (current[0] == null)
                    return false;

                return current[0].older != null;
            }

            @Override
            public synchronized Message next() {
                if (current[0] == null)
                    return null;

                current[0] = current[0].older;
                return current[0].message;
            }
        };
    }

    public @NotNull Iterator<Message> inverseIterator() {
        final Node[] current = { this.oldest };

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                if (current[0] == null)
                    return false;

                return current[0].newer != null;
            }

            @Override
            public synchronized Message next() {
                if (current[0] == null)
                    return null;

                current[0] = current[0].newer;
                return current[0].message;
            }
        };
    }

    public synchronized void add(@NotNull Message message) {
        Node node = new Node(message, this.newest);
        if (this.newest != null)
            this.newest.newer = node;
        this.newest = node;

        this.size++;
        this.trim();
    }

    private void trim() {
        while (this.size > this.capacity) {
            Node secondOldest = this.oldest.newer;

            this.oldest.newer = null;
            this.oldest = secondOldest;
            this.oldest.older = null;
        }
    }
}
