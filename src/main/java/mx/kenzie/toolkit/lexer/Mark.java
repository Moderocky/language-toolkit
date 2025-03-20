package mx.kenzie.toolkit.lexer;

import java.io.Closeable;

public interface Mark extends Closeable, AutoCloseable {

    @Override
    void close();

    void reset();

    void discard();

}

class SimpleMark implements Mark {

    private final TokenStream stream;
    private final boolean discard;
    private boolean closed = false;

    SimpleMark(TokenStream stream, boolean discard) {
        this.stream = stream;
        this.discard = discard;
    }

    @Override
    public void close() {
        if (discard) this.discard();
        else this.reset();
    }

    @Override
    public void reset() {
        if (closed) return;
        this.closed = true;
        this.stream.reset();

    }

    @Override
    public void discard() {
        if (closed) return;
        this.closed = true;
        this.stream.discard();
    }

}
