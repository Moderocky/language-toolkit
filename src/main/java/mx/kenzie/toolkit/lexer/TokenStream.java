package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.lexer.token.Token;

import java.util.Iterator;

public abstract class TokenStream implements Iterator<Token>, Iterable<Token> {

    public TokenStream() {
    }

    @Override
    public abstract boolean hasNext();

    @Override
    public abstract Token next();

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

}
