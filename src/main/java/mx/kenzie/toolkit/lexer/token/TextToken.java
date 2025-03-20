package mx.kenzie.toolkit.lexer.token;

public record TextToken(String value, int line, int position) implements LiteralToken<String> {

    @Override
    public String value() {
        return value;
    }

    @Override
    public String print() {
        char bracket;
        if (!value.contains("\"")) bracket = '"';
        else if (!value.contains("'")) bracket = '\'';
        else bracket = '`';
        StringBuilder builder = new StringBuilder().append(bracket);
        for (char c : value.toCharArray()) {
            if (c == bracket) builder.append("\\");
            switch (c) {
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                case '\f' -> builder.append("\\f");
                case '\b' -> builder.append("\\b");
                case '\\' -> builder.append("\\\\");
                default -> builder.append(c);
            }
        }
        builder.append(bracket);
        return builder.toString();
    }

}
