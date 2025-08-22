package mx.kenzie.toolkit.parser;


import java.util.EmptyStackException;

public interface CallStack {

    static CallStack empty() {
        return Empty.EMPTY;
    }

    Parser peek();

    CallStack pop();

    boolean isEmpty();

    CallStack push(Parser parser);

}

record Empty() implements CallStack {

    static final CallStack EMPTY = new Empty();

    @Override
    public Parser peek() {
        return null;
    }

    @Override
    public CallStack pop() {
        throw new EmptyStackException();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public CallStack push(Parser parser) {
        return new Call(parser, this);
    }

}

record Call(Parser peek, CallStack pop) implements CallStack {

    public boolean isEmpty() {
        return false;
    }

    public CallStack push(Parser parser) {
        return new Call(parser, this);
    }

}
