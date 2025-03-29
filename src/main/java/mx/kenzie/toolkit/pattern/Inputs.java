package mx.kenzie.toolkit.pattern;

import java.util.Collection;
import java.util.Iterator;

public class Inputs implements Iterator<Object> {

    protected final Object[] data;
    protected int index;

    public Inputs(Object... data) {
        this.data = data;
    }

    public Inputs(Collection<?> data) {
        this.data = data.toArray();
    }

    public <Result> Iterable<Result> as(Class<Result> type) {
        return () -> new Iterator<>() {

            @Override
            public boolean hasNext() {
                return Inputs.this.hasNext();
            }

            @Override
            public Result next() {
                return Inputs.this.next(type);
            }
        };
    }

    @Override
    public boolean hasNext() {
        return index < data.length;
    }

    @Override
    public Object next() {
        return data[index++];
    }

    public <Result> Result next(Class<Result> type) {
        return type.cast(this.next());
    }

    public boolean is(Class<?> type) {
        return type.isInstance(data[index]);
    }

    public void reset() {
        this.index = 0;
    }

    public Object peek() {
        return data[index];
    }

    public <Result> Result peek(Class<Result> type) {
        return type.cast(this.peek());
    }

    public int size() {
        return data.length;
    }

}
