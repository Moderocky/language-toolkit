package mx.kenzie.toolkit.lexer.token;

public interface Token {

    String print();

    int line();

    int position();

    default String debugName() {
        final String simpleName = this.getClass().getSimpleName();
        return simpleName.substring(0, simpleName.length() - "Token".length());
    }

}
