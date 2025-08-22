package mx.kenzie.toolkit.parser;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.Tokens;
import mx.kenzie.toolkit.lexer.stream.Mark;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.lexer.token.StructureToken;
import mx.kenzie.toolkit.lexer.token.Token;

public interface BracketedParser extends Parser {

    static Tokens findMatching(Parser outer, TokenStream input, char open, char close)
    throws ParsingException {
        try (Mark mark = input.markForReset()) {
            final StructureToken first = outer.find(StructureToken.class, input);
            if (first.symbol() != open)
                throw new ParsingException("Expected an opening " + open + " bracket, got " + first);
            int count = 1;
            Tokens list = Tokens.simple();
            for (Token token : input) {
                if (token instanceof StructureToken structure) {
                    if (structure.symbol() == open) ++count;
                    if (structure.symbol() == close) --count;
                    if (count == 0) {
                        mark.discard();
                        return list;
                    }
                }
                list.add(token);
            }
            throw new ParsingException("Expected a closing " + close + " bracket matching " + open);
        }
    }

    default Tokens findMatching(TokenStream input, char open, char close)
    throws ParsingException {
        return findMatching(this, input, open, close);
    }

}
