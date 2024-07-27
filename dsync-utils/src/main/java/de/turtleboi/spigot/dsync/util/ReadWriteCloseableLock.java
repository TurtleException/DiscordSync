package de.turtleboi.spigot.dsync.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteCloseableLock {
    private final ReentrantReadWriteLock lock;

    public ReadWriteCloseableLock() {
        lock = new ReentrantReadWriteLock();
    }

    public @NotNull CloseableLock read() {
        return new CloseableLock(this.lock.readLock());
    }

    public @NotNull CloseableLock write() {
        return new CloseableLock(this.lock.writeLock());
    }
}
