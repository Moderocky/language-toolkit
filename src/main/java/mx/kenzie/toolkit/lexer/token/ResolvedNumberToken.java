package mx.kenzie.toolkit.lexer.token;

public record ResolvedNumberToken(String text, Number value, int line, int position) implements NumberToken {

    @Override
    public Number value() {
        return value;
    }

    @Override
    public String print() {
        return text;
    }

}
