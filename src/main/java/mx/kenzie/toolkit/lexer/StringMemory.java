package mx.kenzie.toolkit.lexer;

import java.io.Serializable;

public class StringMemory implements Appendable, Serializable, CharSequence {

    private static final int DEFAULT_BUFFER_SIZE = 64;

    protected char[] buffer;
    protected int position;

    public StringMemory(int initialCapacity) {
        this.buffer = new char[initialCapacity];
    }

    public StringMemory() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public StringMemory(CharSequence csq) {
        this(csq.length());
        this.append(csq);
    }

    protected void require(int amount) {
        if (position + amount > buffer.length) this.grow(amount);
    }

    protected void grow(int expected) {
        expected = buffer.length + expected;
        int amount =
            ((expected / DEFAULT_BUFFER_SIZE) + (expected % DEFAULT_BUFFER_SIZE == 0 ? 0 : 1)) * DEFAULT_BUFFER_SIZE;
        final char[] newBuffer = new char[amount];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        this.buffer = newBuffer;
    }

    @Override
    public StringMemory append(CharSequence csq) {
        final int length = csq.length();
        this.require(length);
        for (int i = 0; i < length; i++) {
            this.buffer[position++] = csq.charAt(i);
        }
        return this;
    }

    @Override
    public StringMemory append(CharSequence csq, int start, int end) {
        final int length = end - start;
        this.require(length);
        for (int i = start; i < end; i++) {
            this.buffer[position++] = csq.charAt(i);
        }
        return this;
    }

    @Override
    public StringMemory append(char c) {
        this.require(1);
        this.buffer[position++] = c;
        return this;
    }

    @Override
    public int length() {
        return position;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index > position) throw new IndexOutOfBoundsException();
        return buffer[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.substring(start, end);
    }

    public String substring(int start, int end) {
        return new String(buffer, start, end - start);
    }

    public String chop(int length) {
        final char[] newBuffer = new char[length];
        System.arraycopy(buffer, position - length, newBuffer, 0, length);
        this.position -= length;
        return new String(newBuffer);
    }

    @Override
    public String toString() {
        return new String(buffer, 0, position);
    }

    public void append(char[] cbuf, int off, int len) {
        System.arraycopy(cbuf, off, buffer, position, len);
        this.position += len;
    }

}
