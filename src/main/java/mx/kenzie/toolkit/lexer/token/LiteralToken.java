package mx.kenzie.toolkit.lexer.token;

public interface LiteralToken<Type> extends Token {

    Type value();

}
