package mx.kenzie.toolkit.lexer.parallel;

import java.util.Iterator;
import java.util.function.Function;

record Linear<Input>(Iterable<Input> values) implements Parallel<Input> {

    @Override
    public <Output> Iterable<Output> spread(Function<Input, Output> function) {
        Iterator<Input> iterator = values.iterator();
        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Output next() {
                return function.apply(iterator.next());
            }
        };
    }

}
