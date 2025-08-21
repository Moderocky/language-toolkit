package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.error.ParsingError;
import mx.kenzie.toolkit.lexer.token.Token;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

class KnownTokenStream extends TokenStream {

    protected final Token[] tokens;
    protected Stack<Integer> marks;
    protected int current;

    public KnownTokenStream(Tokens tokens) {
        super();
        this.tokens = tokens.toArray();
        this.marks = new IntStack();
    }

    @Override
    public boolean hasNext() {
        return current < tokens.length;
    }

    @Override
    public Token next() {
        if (current >= tokens.length)
            throw new ParsingError("Reached the end of available tokens.");
        return tokens[current++];
    }

    @Override
    void mark() {
        this.marks.push(current);
    }

    @Override
    void reset() {
        this.current = marks.pop();
    }

    @Override
    void discard() {
        this.marks.pop();
    }

    @Override
    public void skip() {
        ++this.current;
    }

    @Override
    public Iterator<Token> iterator() {
        return this;
    }

    @Override
    public void revert() {
        --this.current;
    }

    @Override
    public boolean hasAtLeast(int tokens) {
        return current <= (this.tokens.length - tokens);
    }

    @Override
    public Tokens remaining() {
        final Tokens list = new TokenList();
        for (Token token : this) list.add(token);
        return list;
    }

    @Override
    public Position here() {
        if (this.hasNext())
            return Position.of(tokens[current]);
        else if (tokens.length > 0) return Position.of(tokens[tokens.length - 1]);
        else return new Position(0, 0);
    }

    @Override
    public String toString() {
        return "TokenStream[" +
            "remaining=" + Arrays.toString(Arrays.copyOfRange(tokens, current, tokens.length)) +
            ", current=" + current +
            ", marks=" + marks +
            ']';
    }

}
