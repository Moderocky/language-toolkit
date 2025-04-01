package mx.kenzie.toolkit.lexer.token;

public record IntegerToken(String text, Integer value, int line,
                           int position) implements NumberToken {

    @Override
    public String print() {
        return text;
    }

}
