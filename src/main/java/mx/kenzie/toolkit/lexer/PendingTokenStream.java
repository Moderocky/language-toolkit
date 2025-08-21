package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.lexer.concurrent.Block;
import mx.kenzie.toolkit.lexer.token.Token;

import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

class PendingTokenStream extends TokenStream {

    final TokenList tokens;
    protected Stack<TokenList.Node> marks;
    protected TokenList.Node current, previous;
    protected final Lexer lexer;
    protected final Block block;

    public PendingTokenStream(Lexer lexer) {
        this.lexer = lexer;
        this.tokens = lexer.tokens;
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
        this.block = lexer.block();
    }

    void prompt() {
        if (current == null) {
            this.stepSafe();
            synchronized (tokens) {
                if (previous == null) {
                    current = tokens.first;
                } else {
                    current = previous.next;
                }
            }
        }
//        if (current != null && current.next == null)
//            this.stepSafe();
    }

    void stepSafe() {
        block.await();
    }

    @Override
    public boolean hasNext() {
        if (current != null)
            return true;
        this.prompt();
        return current != null;
    }

    @Override
    public Token next() {
        try {
            return current.token;
        } finally {
            previous = current;
            current = current.next;
        }
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
        current = current.next;
    }

    @Override
    public Iterator<Token> iterator() {
        return this;
    }

    @Override
    public void revert() {
        current = current.previous;
    }

    @Override
    public boolean hasAtLeast(int tokens) {
        do {
            int following = 1 + current.following();
            if (following >= tokens) return true;
            this.stepSafe();
        } while (lexer.isAvailable());
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
