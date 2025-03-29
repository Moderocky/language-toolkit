package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.TokenList;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.parser.BracketedParser;
import mx.kenzie.toolkit.parser.Parser;

record Brackets(char open, char close, CharSequence... elements) implements Elements, SubParser {

    @Override
    public String toString() {
        return open + String.join(" ", elements) + close;
    }

    @Override
    public Inputs read(Parser outer, TokenStream input, boolean all) throws ParsingException {
        TokenList list = BracketedParser.findMatching(outer, input, open, close);
        TokenStream inner = new TokenStream(list);
        return SubParser.super.read(outer, inner, true);
    }

}
