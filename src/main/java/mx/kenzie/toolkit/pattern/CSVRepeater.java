package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.Mark;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.lexer.token.WordLikeToken;
import mx.kenzie.toolkit.parser.Parser;

import java.util.ArrayList;
import java.util.List;

record CSVRepeater(CharSequence... elements) implements Elements, SubParser {

    @Override
    public String toString() {
        return String.join(" ", elements) + ", ...";
    }


    @Override
    public Inputs read(Parser outer, TokenStream input, boolean all) throws ParsingException {
        List<Object> list = new ArrayList<>(elements.length);
        do try (Mark mark = input.markForReset()) {
            Inputs read = SubParser.super.read(outer, input, false);
            list.add(read);
            mark.discard();
        } catch (ParsingException ex) {
            break;
        }
        while (this.consumeComma(outer, input) && input.hasNext());
        return new Inputs(list);
    }

    private boolean consumeComma(Parser outer, TokenStream input) throws ParsingException {
        return outer.expect(WordLikeToken.class, input, token -> token.value().equals(","));
    }

}
