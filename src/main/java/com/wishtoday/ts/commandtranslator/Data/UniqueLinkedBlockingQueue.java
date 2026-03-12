package com.wishtoday.ts.commandtranslator.Data;

import java.io.Serial;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class UniqueLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {
    @Serial
    private static final long serialVersionUID = 6523405086129214113L;
    private final ReentrantLock putLock = new ReentrantLock();

    public void put(E e) throws InterruptedException {
        putLock.lock();
        try {
            if (!contains(e)) {
                super.put(e);
            }
        } finally {
            putLock.unlock();
        }
    }
}
