package mx.kenzie.toolkit.lexer.token;

public record WordToken(String value, int line, int position) implements WordLikeToken, Token {

    @Override
    public String print() {
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isWhitespace(c)) builder.append('\\');
            builder.append(c);
        }
        return builder.toString();
    }

}
