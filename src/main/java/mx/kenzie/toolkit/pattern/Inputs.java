package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.lexer.Position;

import java.util.Collection;
import java.util.Iterator;

public class Inputs implements Iterator<Object> {

    protected final Position position;
    protected final Object[] data;
    protected int index;

    public Inputs(Position position, Object... data) {
        this.position = position;
        this.data = data;
    }

    public Inputs(Object... data) {
        this(Position.NOWHERE, data);
    }

    public Inputs(Position position, Collection<?> data) {
        this(position, data.toArray());
    }

    public Inputs(Collection<?> data) {
        this(Position.NOWHERE, data);
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
