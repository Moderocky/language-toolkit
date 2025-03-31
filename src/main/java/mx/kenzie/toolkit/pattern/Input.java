package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.lexer.Position;

import java.util.Collection;
import java.util.Iterator;

public class Input implements Iterator<Object> {

    protected final Position position;
    protected final Object[] data;
    protected int index;

    public Input(Position position, Object... data) {
        this.position = position;
        this.data = data;
    }

    public Input(Object... data) {
        this(Position.NOWHERE, data);
    }

    public Input(Position position, Collection<?> data) {
        this(position, data.toArray());
    }

    public Input(Collection<?> data) {
        this(Position.NOWHERE, data);
    }

    public Input append(Object... data) {
        Object[] newData = new Object[this.data.length + data.length];
        System.arraycopy(this.data, 0, newData, 0, this.data.length);
        System.arraycopy(data, 0, newData, this.data.length, data.length);
        return new Input(this.position, newData);
    }

    public Input prepend(Position position, Object... data) {
        Object[] newData = new Object[this.data.length + data.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        System.arraycopy(this.data, 0, newData, data.length, this.data.length);
        return new Input(this.position, newData);
    }

    public <Result> Iterable<Result> as(Class<Result> type) {
        return () -> new Iterator<>() {

            @Override
            public boolean hasNext() {
                return Input.this.hasNext();
            }

            @Override
            public Result next() {
                return Input.this.next(type);
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

    public Position position() {
        return position;
    }

}
