package mx.kenzie.toolkit.lexer.stream;

import java.io.Closeable;
import java.util.function.Consumer;

public interface Mark extends Closeable, AutoCloseable {

    @Override
    void close();

    void reset();

    void discard();

}

class ForkingMark<Stream extends TokenStream> implements Mark, Consumer<TokenStream> {

    private final Stream stream;
    private final Consumer<Stream> resetter;
    private boolean closed = false;

    ForkingMark(Stream stream, Consumer<Stream> resetter) {
        this.stream = stream;
        this.resetter = resetter;
    }

    @Override
    public void close() {
        this.discard();
    }

    @Override
    public void reset() {
        if (closed) return;
        this.closed = true;
        this.resetter.accept(stream);
    }

    @Override
    public void discard() {
        if (closed) return;
        this.closed = true;
    }

    @Override
    public void accept(TokenStream stream) {
        this.closed = true;
        this.resetter.accept((Stream) stream);
    }

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
