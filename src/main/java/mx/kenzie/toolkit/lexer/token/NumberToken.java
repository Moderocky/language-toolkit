package mx.kenzie.toolkit.lexer.token;

public interface NumberToken extends LiteralToken<Number> {

    @Override
    Number value();

}
