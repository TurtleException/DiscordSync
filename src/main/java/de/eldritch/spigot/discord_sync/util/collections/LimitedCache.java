package de.eldritch.spigot.discord_sync.util.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

public class LimitedCache<E> implements Collection<E> {
    private final int capacity;

    protected final Object lock = new Object();

    private Node head;
    private Node tail;
    private int size = 0;

    public LimitedCache(int capacity) {
        this.capacity = capacity;
    }

    private class Node {
        final E obj;
        Node next = null;

        public Node(@NotNull E obj) {
            this.obj = obj;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size <= 0;
    }

    @Override
    public boolean contains(Object o) {
        if (head == null)
            return false;

        for (E obj : this)
            if (obj.equals(o)) return true;

        return false;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node node = head;

            @Override
            public boolean hasNext() {
                return node.next != null;
            }

            @Override
            public E next() {
                node = node.next;
                return node.obj;
            }
        };
    }

    @Override
    public @NotNull Object[] toArray() {
        synchronized (lock) {
            Object[] arr = new Object[size];
            int i = 0;
            for (E e : this) {
                arr[i] = e;
                i++;
            }
            return arr;
        }
    }

    @Override
    public <T> @NotNull T[] toArray(@NotNull T[] a) {
        return a;
    }

    @Override
    public boolean add(E e) {
        synchronized (lock) {
            // SPECIAL CASE: cache is empty
            if (head == null) {
                head = new Node(e);
                tail = head;
                size++;
                return true;
            }

            // append element
            tail.next = new Node(e);
            tail = tail.next;

            // check capacity
            if (++size > capacity) {
                head = head.next;
                size--;
            }

            return true;
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (lock) {
            // SPECIAL CASE: object is head
            if (head.equals(o)) {
                head = head.next;
                size--;
                return true;
            }

            Node node = head;
            while (node != null) {
                if (node.next != null && node.next.equals(o)) {
                    node.next = node.next.next;
                    size--;
                    return true;
                }

                node = node.next;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c)
            if (!contains(o))
                return false;
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean b = false;
        for (E e : c)
            if (add(e))
                b = true;
        return b;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean b = false;
        for (Object o : c)
            if (remove(o))
                b = true;
        return b;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        synchronized (lock) {
            head = null;
            tail = null;
            size = 0;
        }
    }
}
