package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.lexer.token.Token;

import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

class PendingTokenStream extends TokenStream {

    final TokenList tokens;
    protected Stack<TokenList.Node> marks;
    protected TokenList.Node current, previous;
    protected final Lexer lexer;
    protected boolean done;

    public PendingTokenStream(Lexer lexer) {
        this.lexer = lexer;
        this.tokens = new TokenList();
        this.current = tokens.first;
        this.marks = new Stack<>();
        Thread.ofVirtual().start(() -> {
            try {
                do lexer.step();
                while (lexer.available);
            } catch (IOException e) {
                lexer.markClosed();
            }
        });
    }

    protected void setup() {
        if (done || current != null || previous != null)
            return;
        this.poll();
    }

    @Override
    public boolean hasNext() {
        return current != null || this.poll();
    }

    protected boolean poll() {
        if (done) return false;
        Token polled;
        while ((polled = lexer.tokens.poll()) != null) {
            this.tokens.add(polled);
            if (previous == null) current = tokens.first;
            else current = previous.next;
        }
        if (current == null) {
            polled = lexer.poll();
            if (polled == Lexer.END) {
                done = true;
                return false;
            }
            this.tokens.add(polled);
            if (previous == null) current = tokens.first;
            else current = previous.next;
        }
        return true;
    }

    @Override
    public Token next() {
        try {
            return current.token;
        } finally {
            previous = current;
            if (current != null)
                current = current.next;
        }
    }

    @Override
    void mark() {
        this.setup();
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
        this.setup();
        if (current == null) throw new IllegalStateException();
        current = current.next;
    }

    @Override
    public Iterator<Token> iterator() {
        return this;
    }

    @Override
    public void revert() {
        this.setup();
        if (current == null) throw new IllegalStateException();
        current = current.previous;
    }

    @Override
    public boolean hasAtLeast(int tokens) {
        do {
            int following = 1 + current.following();
            if (following >= tokens) return true;
            this.poll();
        } while (!done);
        return false;
    }

    @Override
    public Tokens remaining() {
        return tokens.from(current);
    }

    @Override
    public Position here() {
        if (current != null || this.hasNext())
            return Position.of(current.token);
        else if (!tokens.isEmpty()) return Position.of(tokens.last.token);
        else return new Position(0, 0);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        boolean first = true;
        TokenList.Node current = this.current;
        while (current != null) {
            if (first) first = false;
            else builder.append(',');
            builder.append(current.token);
            current = current.next;
        }
        if (lexer.isAvailable())
            builder.append(", ...");
        builder.append(']');
        return builder.toString();
    }

}
