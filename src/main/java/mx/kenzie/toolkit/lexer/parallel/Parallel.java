package mx.kenzie.toolkit.lexer.parallel;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public interface Parallel<Input> {

    static <Input> Parallel<Input> inPlace(Iterable<Input> iterable) {
        return new Linear<>(iterable);
    }

    static <Input> Parallel<Input> deferred(Iterable<Input> iterable, ExecutorService executor) {
        return new Threaded<>(iterable, executor);
    }

    <Output> Iterable<Output> spread(Function<Input, Output> function);

}
