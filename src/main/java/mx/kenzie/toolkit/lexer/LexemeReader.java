package mx.kenzie.toolkit.lexer;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Stack;

public class LexemeReader extends Reader {

    protected final PushbackReader reader;

    protected int position;
    protected int line;
    protected Stack<Integer> marks;
    protected StringMemory buffer;
    protected boolean closed;

    public LexemeReader(Reader reader) {
        if (reader instanceof LexemeReader ours) this.reader = ours.reader;
        else if (reader instanceof PushbackReader push) this.reader = push;
        else this.reader = new PushbackReader(reader, 16);
        this.marks = new IntStack();
    }

    public void discard() {
        this.marks.pop();
    }

    @Override
    public int read() throws IOException {
        final int c = reader.read();
        if (c != -1) ++position;
        if (c == '\n') ++line;
        if (!marks.isEmpty()) buffer.append((char) c);
        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        this.position += len;
        int read = reader.read(cbuf, off, len);
        if (!marks.isEmpty()) buffer.append(cbuf, off, len);
        return read;
    }

    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (buffer == null) buffer = new StringMemory(readAheadLimit);
        else buffer.require(readAheadLimit);
        this.marks.push(position);
    }

    @Override
    public void reset() throws IOException {
        int here = position, length;
        this.position = marks.pop();
        length = here - position;
        String chop = buffer.chop(length);
        this.reader.unread(chop.toCharArray());
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
        this.closed = true;
    }

    public int position() {
        return position;
    }

    public int line() {
        return line;
    }

    public void unread(String string) throws IOException {
        if (!marks.isEmpty()) buffer.chop(string.length());
        this.reader.unread(string.toCharArray());
    }

}
