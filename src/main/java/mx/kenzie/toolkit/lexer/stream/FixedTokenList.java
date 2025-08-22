package mx.kenzie.toolkit.lexer.stream;

import mx.kenzie.toolkit.lexer.Tokens;
import mx.kenzie.toolkit.lexer.token.Token;
import mx.kenzie.toolkit.lexer.token.WhitespaceToken;

import java.util.ArrayList;
import java.util.Collection;

public class FixedTokenList extends ArrayList<Token> implements Tokens {

    protected boolean ignoreWhitespace;

    public FixedTokenList() {
    }

    public FixedTokenList(Collection<? extends Token> c) {
        super(c);
    }

    @Override
    public void ignoreWhitespace() {
        this.removeIf(token -> token instanceof WhitespaceToken);
        this.ignoreWhitespace = true;
    }

    @Override
    public Token first() {
        return this.getFirst();
    }

    @Override
    public Token last() {
        return this.getLast();
    }

    @Override
    public Token[] toArray() {
        return this.toArray(new Token[0]);
    }

    @Override
    public boolean add(Token token) {
        if (ignoreWhitespace && token instanceof WhitespaceToken)
            return false;
        return super.add(token);
    }

}
