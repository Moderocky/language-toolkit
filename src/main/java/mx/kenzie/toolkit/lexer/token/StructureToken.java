package mx.kenzie.toolkit.lexer.token;

public record StructureToken(char symbol, int line, int position) implements WordLikeToken, Token {

    @Override
    public String print() {
        return Character.toString(symbol);
    }

    @Override
    public String value() {
        return Character.toString(symbol);
    }

}
