package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.error.ReadingException;
import mx.kenzie.toolkit.lexer.concurrent.Block;
import mx.kenzie.toolkit.lexer.token.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class Lexer {

    protected static final int BUMP = '\uFFFF' + 1;
    protected static final int ESCAPE = '\\';

    protected final LexemeReader reader;
    final TokenList tokens;
    protected volatile boolean available = true;
    private boolean wasNotNumber;
    final Block block = new Block();

    public Lexer(Reader reader) {
        if (reader instanceof LexemeReader ours) this.reader = ours;
        else this.reader = new LexemeReader(reader);
        this.tokens = new TokenList();
//        assert reader.markSupported();
    }

    protected Block block() {
        return block;
    }

    protected void addToken(Token token) {
        synchronized (tokens) {
            tokens.add(token);
        }
        block.wake();
    }

    void markClosed() {
        synchronized (this) {
            available = false;
        }
        block.complete();
    }

    public Tokens run() throws IOException {
        if (!available) return tokens;
        do this.step();
        while (this.isAvailable());
        return tokens;
    }

    public TokenStream live() {
        PendingTokenStream stream = new PendingTokenStream(this);
        stream.tokens.removeWhitespace();
        return stream;
    }

    public TokenStream live(boolean preserveWhitespace) {
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
