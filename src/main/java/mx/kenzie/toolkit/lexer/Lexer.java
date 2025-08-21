package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.error.ReadingException;
import mx.kenzie.toolkit.lexer.token.*;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Lexer {

    protected static final int BUMP = '\uFFFF' + 1;
    protected static final int ESCAPE = '\\';
    protected static final Token END = new Token() {
        @Override
        public String print() {
            return "END";
        }

        @Override
        public int line() {
            return -1;
        }

        @Override
        public int position() {
            return -1;
        }
    };

    protected final LexemeReader reader;
    Queue<Token> tokens;
    protected volatile boolean available = true;
    private boolean wasNotNumber;

    public Lexer(Reader reader) {
        if (reader instanceof LexemeReader ours) this.reader = ours;
        else this.reader = new LexemeReader(reader);
    }

    protected void addToken(Token token) {
        tokens.add(token);
    }

    void markClosed() {
        synchronized (this) {
            available = false;
        }
        if (tokens instanceof BlockingQueue)
            tokens.add(END);
    }

    Token poll() {
        if (tokens instanceof BlockingQueue<Token> queue) {
            try {
                if (this.isAvailable())
                    return queue.take();
                if (queue.isEmpty()) {
                    return END;
                }
                return queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return END;
        } else {
            return tokens.poll();
        }
    }

    public Tokens run() throws IOException {
        if (tokens != null) throw new IllegalStateException();
        this.tokens = new LinkedList<>();
        if (!available) return Tokens.from(tokens);
        do this.step();
        while (available);
        return Tokens.from(tokens);
    }

    public TokenStream live() {
        if (tokens != null) throw new IllegalStateException();
        this.tokens = new LinkedBlockingQueue<>();
        PendingTokenStream stream = new PendingTokenStream(this);
        stream.tokens.removeWhitespace();
        return stream;
    }

    public TokenStream live(boolean preserveWhitespace) {
        if (tokens != null) throw new IllegalStateException();
        this.tokens = new LinkedBlockingQueue<>();
        if (preserveWhitespace)
            return new PendingTokenStream(this);
        return this.live();
    }

    public synchronized boolean isAvailable() {
        return available;
    }

    protected void step() throws IOException {
        this.readWhitespace();
        this.readWord();
    }

    protected void readWhitespace() throws IOException {
        boolean anyFound = false;
        int line = reader.line();
        int position = reader.position();
        do {
            this.reader.mark(4);
            final int c = this.reader.read();
            if (c == -1) {
                this.markClosed();
                break;
            } else if (Character.isWhitespace(c)) anyFound = true;
            else {
                this.reader.reset();
                break;
            }
        } while (available);
        if (anyFound) this.addToken(new WhitespaceToken(line, position));
    }

    protected void readWord() throws IOException {
        final int c = this.reader.read();
        if (c == -1) {
            this.markClosed();
            return;
        }
        if (wasNotNumber) wasNotNumber = false;
        switch (c) {
            case '{', '}', ';', '(', ')', '[', ']', '<', '>', ',', '.' -> this.readStructure((char) c);
            case '"', '\'' -> this.readString((char) c);
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> this.readNumber((char) c);
            default -> this.readToken((char) c);
        }
    }

    protected boolean readNumber(char start) throws IOException {
        final StringBuilder builder = new StringBuilder();
        int line = reader.line();
        int position = reader.position() - 1;
        builder.append(start);
        boolean hasDot = false;
        this.reader.mark(32);
        do {
            this.reader.mark(2);
            final int c = this.reader.read();
            if (c == -1) {
                this.markClosed();
                break;
            }
            if (Character.isDigit(c) || c == '_') {
                builder.append((char) c);
            } else if (!hasDot && c == '.') {
                hasDot = true;
                builder.append((char) c);
            } else {
                this.reader.reset();
                break;
            }
        } while (available);
//        if (builder.toString().equals("-")) {
//            this.wasNotNumber = true;
//            this.reader.reset();
//            return false;
//        }
        if (builder.charAt(builder.length() - 1) == '.') {
            this.reader.unread(".");
            builder.deleteCharAt(builder.length() - 1);
        }
        final String text = builder.toString();
        if (hasDot)
            this.addToken(new ResolvedNumberToken(text, Double.valueOf(text), line, position));
        else
            this.addToken(new IntegerToken(text, Integer.valueOf(text), line, position));
        return true;
    }

    protected void readToken(char start) throws IOException {
        final StringBuilder builder = new StringBuilder();
        int line = reader.line();
        int position = reader.position() - 1;
        int nonLetters = this.isWordChar(start) ? 0 : 1;
        boolean escape = false;
        builder.append(start);
        do {
            this.reader.mark(1);
            int c = this.reader.read();
            if (c == -1) {
                this.markClosed();
                break;
            }
            if (c == '\n' && escape) {
                this.reader.reset();
                throw new ReadingException("Tried to escape new line in word token.", reader.line(),
                    reader.position() - 1);
            }
            if (escape) c = c + BUMP;
            if (c == ESCAPE) {
                escape = true;
                nonLetters++;
            } else if (Character.isWhitespace(c) || this.isIllegalWordChar(c)) {
                this.reader.reset();
                break;
            } else if (nonLetters > 1 && !escape && this.isWordChar(c)) {
                this.reader.reset();
                break;
            } else if (nonLetters > 0 && this.isWordChar(c)) {
                this.reader.reset();
                break;
            } else if (this.isWordChar(c)) {
                nonLetters = 0;
                builder.append((char) c);
                if (escape) escape = false;
            } else if (nonLetters > 0) {
                nonLetters++;
                builder.append((char) c);
                if (escape) escape = false;
            } else {
                this.reader.reset();
                break;
            }
        } while (available);
        this.addToken(new WordToken(builder.toString(), line, position));
    }

    protected boolean isIllegalWordChar(int c) {
        return c == '`' || c == '}' || c == ')' || c == ']';
    }

    protected boolean isWordChar(int c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    protected void readStructure(char c) throws IOException {
        this.addToken(new StructureToken(c, reader.line(), reader.position() - 1));
    }

    protected void readString(char end) throws IOException {
        boolean escape = false;
        final StringBuilder builder = new StringBuilder();
        int line = reader.line();
        int position = reader.position() - 1;
        do {
            int c = this.reader.read();
            if (c == -1)
                throw new ReadingException("Reached end of file inside text.", reader.line(), reader.position() - 1);
            if (escape) c = c + BUMP;
            if (c == ESCAPE) escape = true;
            else if (c == end) break;
            else if (c == ('r' + BUMP)) builder.append('\r');
            else if (c == ('n' + BUMP)) builder.append('\n');
            else if (c == ('t' + BUMP)) builder.append('\t');
            else if (c == ('b' + BUMP)) builder.append('\b');
            else if (c == ('f' + BUMP)) builder.append('\f');
            else {
                builder.append((char) c);
                if (escape) escape = false;
            }
        } while (available);
        this.addToken(new TextToken(builder.toString(), line, position));
    }

}
