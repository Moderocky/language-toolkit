package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.lexer.token.ResolvedNumberToken;
import mx.kenzie.toolkit.lexer.token.WordLikeToken;
import mx.kenzie.toolkit.parser.Parser;

public interface Taker extends CharSequence {

    Object take(TokenStream stream, Parser outer) throws ParsingException;

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

class Word implements Taker {

    @Override
    public Object take(TokenStream stream, Parser outer) throws ParsingException {
        return outer.find(WordLikeToken.class, stream).value();
    }

    @Override
    public String toString() {
        return "<word>";
    }

}

class Number implements Taker {

    @Override
    public Object take(TokenStream stream, Parser outer) throws ParsingException {
        return outer.find(ResolvedNumberToken.class, stream).value();
    }

    @Override
    public String toString() {
        return "<number>";
    }

}
