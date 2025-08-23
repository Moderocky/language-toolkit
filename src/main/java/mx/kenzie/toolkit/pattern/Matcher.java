package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.lexer.stream.Mark;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.lexer.token.Token;
import mx.kenzie.toolkit.lexer.token.WordLikeToken;

public interface Matcher extends CharSequence {

    boolean match(TokenStream input);

    @Override
    default int length() {
        return this.toString().length();
    }

    @Override
    default char charAt(int index) {
        return this.toString().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return this.toString().subSequence(start, end);
    }

}

record Optional(String word) implements Matcher {

    @Override
    public boolean match(TokenStream input) {
        if (!input.hasNext())
            return true;
        try (Mark mark = input.markForReset()) {
            Token next = input.next();
            if (next instanceof WordLikeToken token && token.value().equals(word))
                mark.discard();
        }
        return true;
    }

    @Override
    public String toString() {
        return "maybe(" + word + ")";
    }

}

record Choice(String... words) implements Matcher {

    @Override
    public boolean match(TokenStream input) {
        if (words.length == 0) return true;
        if (!input.hasNext()) return false;
        if (input.next() instanceof WordLikeToken token) {
            for (String word : words) {
                if (word.equals(token.value()))
                    return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "any(" + String.join(", ", words) + ")";
    }

}
