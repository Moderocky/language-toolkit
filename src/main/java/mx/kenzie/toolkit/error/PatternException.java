package mx.kenzie.toolkit.error;

public class PatternException extends RuntimeException {

    public PatternException() {
        super();
    }

    public PatternException(String message) {
        super(message);
    }

    public PatternException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatternException(Throwable cause) {
        super(cause);
    }

}
