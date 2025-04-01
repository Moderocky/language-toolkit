package mx.kenzie.toolkit.lexer.token;

public record DecimalToken(String text, char[] raw, Number value, int line,
                           int position) implements NumberToken {

    public DecimalToken(String text, char[] raw, int line, int position) {
        this(text, raw, Integer.parseInt(text), line, position);
    }

    public Integer asInteger() {
        return value.intValue();
    }

    public Number asDecimal() {
        return value.intValue() / (double) raw.length;
    }

    public Number appendDecimal(Number value) {
        return value.intValue() + this.asDecimal().doubleValue();
    }

    @Override
    public String print() {
        return text;
    }

}
