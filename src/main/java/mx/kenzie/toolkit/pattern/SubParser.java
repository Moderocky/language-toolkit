package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.Position;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.lexer.token.WordLikeToken;
import mx.kenzie.toolkit.parser.Parser;
import mx.kenzie.toolkit.parser.Unit;

import java.util.ArrayList;
import java.util.List;

interface SubParser {

    default Input read(Parser outer, TokenStream input, boolean all) throws ParsingException {
        return this.read(this.elements(), outer, input, all);
    }

    default Input read(CharSequence[] elements, Parser outer, TokenStream input, boolean all) throws ParsingException {
        Position position = input.here();
        int remaining = elements.length;
        List<Object> parts = new ArrayList<>(remaining);
        for (CharSequence element : elements) {
            --remaining;
            this.parse(parts, element, outer, input, all && remaining == 0);
        }
        if (all && input.hasNext())
            throw new ParsingException("Too many tokens remaining for '" + this + "': " + input.remaining());
        return new Input(position, parts);
    }

    default void parse(List<Object> results, CharSequence element, Parser outer, TokenStream input, boolean all)
    throws ParsingException {
        switch (element) {
            case String string -> {
                WordLikeToken word = outer.find(WordLikeToken.class, input);
                if (!word.value().equals(string))
                    throw new ParsingException("Expected '" + string + "' but got '" + word.value() + "'");
            }
            case Taker taker -> results.add(taker.take(input, outer));
            case SubParser thing -> results.add(thing.read(outer, input, all));
            case Unit unit -> results.add(outer.parse(outer, unit, input, all));
            default -> throw new IllegalStateException("Unexpected pattern element: " + element);
        }
    }

    CharSequence[] elements();

}
