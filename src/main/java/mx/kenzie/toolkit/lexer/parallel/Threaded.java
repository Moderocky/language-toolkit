package mx.kenzie.toolkit.lexer.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

record Threaded<Input>(Iterable<Input> values,
                       ExecutorService service) implements Parallel<Input> {

    @Override
    public <Output> Iterable<Output> spread(Function<Input, Output> function) {
        Collection<Supplier<Output>> futures = new ArrayList<>();
        for (Input value : values) {
            Pending<Output> pending = new Pending<>();
            futures.add(pending);
            service.submit(() -> {
                try {
                    pending.complete(function.apply(value));
                } catch (Exception e) {
                    pending.complete(null);
                }
            });
        }

        Iterator<Supplier<Output>> iterator = futures.iterator();
        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Output next() {
                return iterator.next().get();
            }
        };
    }

}

class Pending<Output> implements Supplier<Output> {

    volatile boolean complete;
    volatile Output value;

    @Override
    public Output get() {
        do {
            try {
                synchronized (this) {
                    if (complete)
                        return value;
                    this.wait();
                }
            } catch (InterruptedException _) {
            }
        } while (true);
    }

    void complete(Output value) {
        synchronized (this) {
            if (complete) return;
            this.value = value;
            this.complete = true;
            this.notifyAll();
        }
    }

}
