package mx.kenzie.toolkit.lexer.concurrent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Block {

    private final Object lock = new Object();
    private volatile boolean complete = true;

    public void await() {
        synchronized (lock) {
            if (this.isComplete()) return;
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new Interrupt();
            }
        }
    }

    public void await(Supplier<Boolean> condition) {
        while (!condition.get()) {
            this.await();
        }
    }

    public void wake() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public synchronized boolean isComplete() {
        return complete;
    }

    public synchronized void complete() {
        this.complete = true;
        this.wake();
    }

}
