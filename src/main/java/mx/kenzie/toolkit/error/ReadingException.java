package mx.kenzie.toolkit.error;

import java.io.IOException;

public class ReadingException extends IOException {

    protected final int line, position;

    public ReadingException(String message, int line, int position) {
        super(message);
        this.line = line;
        this.position = position;
    }

    public ReadingException(String message, Throwable cause, int line, int position) {
        super(message, cause);
        this.line = line;
        this.position = position;
    }

}
