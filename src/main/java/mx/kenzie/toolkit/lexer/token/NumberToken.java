package mx.kenzie.toolkit.lexer.token;

public record NumberToken(String text, Number value, int line, int position) implements LiteralToken<Number> {

    @Override
    public Number value() {
        return value;
    }

    @Override
    public String print() {
        return text;
    }

}
