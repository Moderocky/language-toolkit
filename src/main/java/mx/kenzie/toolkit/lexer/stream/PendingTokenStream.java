package mx.kenzie.toolkit.lexer.stream;

import mx.kenzie.toolkit.lexer.Lexer;
import mx.kenzie.toolkit.lexer.Position;
import mx.kenzie.toolkit.lexer.Tokens;
import mx.kenzie.toolkit.lexer.parallel.Parallel;
import mx.kenzie.toolkit.lexer.token.Token;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PendingTokenStream extends TokenStream {

    private static final ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
    protected final Lexer lexer;
    final TokenList tokens;
    protected Deque<TokenList.Node> marks;
    protected TokenList.Node current;
    protected boolean started;

    PendingTokenStream(PendingTokenStream parent) {
        this.tokens = parent.tokens;
        this.lexer = parent.lexer;
        this.marks = new ArrayDeque<>(parent.marks);
    }

    public PendingTokenStream(Lexer lexer, TokenList tokens) {
        this.lexer = lexer;
        this.tokens = tokens;
        this.marks = new ArrayDeque<>();
        service.submit(() -> {
            try {
                lexer.consumeAll();
            } catch (IOException e) {
                // todo
            }
        });
        this.current = tokens.first;
    }

    protected void setup() {
        if (!started) {
            current = tokens.awaitFirstNode();
            started = true;
        }
    }

    @Override
    public TokenStream fork() {
        return new PendingForked();
    }

    @Override
    public boolean hasNext() {
        this.setup();
        if (!started) {
            current = tokens.awaitFirstNode();
            started = true;
        }
        return current != null;
    }

    @Override
    public Token next() {
        try {
            return current.token;
        } finally {
            if (current != null)
                current = current.next();
        }
    }

    @Override
    public Consumer<TokenStream> forkPoint() {
        final var here = current;
        final var marks = new ArrayList<>(this.marks);
        final var started = this.started;
        ForkingMark<PendingTokenStream> mark = new ForkingMark<>(this, (stream) -> {
            stream.current = here;
            stream.marks = new ArrayDeque<>(marks);
            stream.started = started;
        });
        return mark;
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
        current = current.next();
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
        } while (!this.tokens.done);
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
        else if (!tokens.isEmpty()) return Position.of(tokens.last());
        else return new Position(0, 0);
    }

    @Override
    public <Input> Parallel<Input> parallel(Iterable<Input> iterable) {
        return Parallel.deferred(iterable, service);
    }

    protected void poll() {
        tokens.poll();
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

    class PendingForked extends PendingTokenStream {

        public PendingForked() {
            super(PendingTokenStream.this);
            this.current = PendingTokenStream.this.current;
            this.started = PendingTokenStream.this.started;
        }

    }

}
