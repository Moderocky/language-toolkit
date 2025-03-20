package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.lexer.token.Token;

public record Position(int line, int position) {

    public static final Position NOWHERE = new Position(-1, -1);

    public static Position of(Token token) {
        if (token == null) return NOWHERE;
        return new Position(token.line(), token.position());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Position;
    }

}
