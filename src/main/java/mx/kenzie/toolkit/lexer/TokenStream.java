package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.lexer.token.Token;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

public class TokenStream implements Iterator<Token>, Iterable<Token> {

    protected final Token[] tokens;
    protected int current;
    protected Stack<Integer> marks;

    public TokenStream(TokenList tokens) {
        this.tokens = tokens.toArray(new Token[0]);
        this.marks = new Stack<>();
    }

    @Override
    public boolean hasNext() {
        return current < tokens.length;
    }

    @Override
    public Token next() {
        return tokens[current++];
    }

    public Mark markForReset() {
        this.mark();
        return new SimpleMark(this, false);
    }

    public Mark markForDiscard() {
        this.mark();
        return new SimpleMark(this, true);
    }

    void mark() {
        this.marks.push(current);
    }

    void reset() {
        this.current = marks.pop();
    }

    void discard() {
        this.marks.pop();
    }

    public void skip() {
        ++this.current;
    }

    @Override
    public Iterator<Token> iterator() {
        return this;
    }

    @Override
    public String toString() {
        return "TokenStream[" +
            "remaining=" + Arrays.toString(Arrays.copyOfRange(tokens, current, tokens.length)) +
            ", current=" + current +
            ", marks=" + marks +
            ']';
    }

    public void revert() {
        --this.current;
    }

    public boolean hasAtLeast(int tokens) {
        return current <= (this.tokens.length - tokens);
    }

    public TokenList remaining() {
        final TokenList list = new TokenList();
        for (Token token : this) list.add(token);
        return list;
    }

    public Position here() {
        if (this.hasNext())
            return Position.of(tokens[current]);
        else if (tokens.length > 0) return Position.of(tokens[tokens.length - 1]);
        else return new Position(0, 0);
    }

}
