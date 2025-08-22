package mx.kenzie.toolkit.lexer.stream;

class Block {

    private final Object lock = new Object();
    private volatile boolean sleeping = true;

    public void await() {
        do {
            try {
                synchronized (lock) {
                    if (!sleeping)
                        return;
                    lock.wait();
                }
                break;
            } catch (InterruptedException _) {
            }
        } while (true);
    }

    public void complete() {
        synchronized (lock) {
            sleeping = false;
            lock.notifyAll();
        }
    }

    public void reset() {
        synchronized (lock) {
            sleeping = true;
        }
    }

}
