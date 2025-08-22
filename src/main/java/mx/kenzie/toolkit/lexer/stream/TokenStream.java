package mx.kenzie.toolkit.lexer.stream;

import mx.kenzie.toolkit.lexer.Position;
import mx.kenzie.toolkit.lexer.Tokens;
import mx.kenzie.toolkit.lexer.parallel.Parallel;
import mx.kenzie.toolkit.lexer.token.Token;

import java.util.Iterator;
import java.util.function.Consumer;

public abstract class TokenStream implements Iterator<Token>, Iterable<Token> {

    public TokenStream() {
    }

    public abstract TokenStream fork();

    @Override
    public abstract boolean hasNext();

    @Override
    public abstract Token next();

    public abstract Consumer<TokenStream> forkPoint();

    public Mark markForReset() {
        this.mark();
        return new SimpleMark(this, false);
    }

    public Mark markForDiscard() {
        this.mark();
        return new SimpleMark(this, true);
    }

    abstract void mark();

    abstract void reset();

    abstract void discard();

    public abstract void skip();

    @Override
    public abstract Iterator<Token> iterator();

    public abstract void revert();

    public abstract boolean hasAtLeast(int tokens);

    public abstract Tokens remaining();

    public abstract Position here();

    public <Input> Parallel<Input> parallel(Iterable<Input> iterable) {
        return Parallel.inPlace(iterable);
    }

}
