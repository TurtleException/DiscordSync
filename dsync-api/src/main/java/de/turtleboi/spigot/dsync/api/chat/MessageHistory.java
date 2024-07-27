package de.turtleboi.spigot.dsync.api.chat;

import de.turtleboi.spigot.dsync.api.entity.Message;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MessageHistory implements Queue<Message>, Set<Message> {
    private static class Node {
        private final Message message;
        private Node prev;
        private Node next;

        public Node(Message message, Node prev) {
            this.message = message;
            this.prev = prev;
        }
    }

    private Node head;
    private Node tail;

    public MessageHistory() {
        // TODO: capacity
    }

    @SuppressWarnings("ReplaceInefficientStreamCount")
    @Override
    public int size() {
        return (int) this.stream().count();
    }

    @Override
    public boolean isEmpty() {
        return this.head == null;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Message)) return false;

        for (Message message : this)
            if (message.equals(o))
                return true;
        return false;
    }

    @Override
    public @NotNull Iterator<Message> iterator() {
        final Node[] current = { this.head };

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return current[0].next != null;
            }

            @Override
            public Message next() {
                current[0] = current[0].next;
                return current[0].message;
            }
        };
    }

    @SuppressWarnings({"SimplifyStreamApiCallChains", "NullableProblems"})
    @Override
    public @NotNull Object[] toArray() {
        return this.stream().toArray();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public <T> @NotNull T[] toArray(@NotNull T[] a) {
        return this.stream().toList().toArray(a);
    }

    @Override
    public boolean add(Message message) {
        if (message == null)
            return false;

        this.head = new Node(message, this.head);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        List<?> l = new ArrayList<>(c);

        for (Message message : this)
            l.remove(message);

        return l.isEmpty();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Message> c) {
        boolean b = false;
        for (Message message : c)
            b = this.add(message) | b;
        return b;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean b = false;
        for (Object o : c)
            b = this.remove(o) | b;
        return b;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean b = false;

        Node current = this.head;
        while (current != null) {
            if (!c.contains(current.message)) {
                if (current.prev != null) {
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                } else {
                    current.next.prev = null;
                }

                b = true;
            }

            current = current.next;
        }

        return b;
    }

    @Override
    public void clear() {
        this.head = null;
        this.tail = null;
        Runtime.getRuntime().gc();
    }

    @Override
    public boolean offer(Message message) {
        this.add(message);
        return true;
    }

    @Override
    public Message remove() {
        Message m = this.poll();
        if (m == null)
            throw new NoSuchElementException();
        return m;
    }

    @Override
    public Message poll() {
        if (this.tail == null)
            return null;

        Node n = this.tail;
        if (this.tail.prev != null) {
            this.tail = this.tail.prev;
            this.tail.next = null;
        } else {
            this.tail = null;
        }
        return n.message;
    }

    @Override
    public Message element() throws NoSuchElementException {
        if (this.tail == null)
            throw new NoSuchElementException();
        return this.tail.message;
    }

    @Override
    public Message peek() {
        if (this.tail == null)
            return null;
        return this.tail.message;
    }
}
