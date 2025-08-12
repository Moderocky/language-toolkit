package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.parser.Parser;

record Limited<Type extends SubParser & Elements>(Type inner, int limit) implements Elements, SubParser {


    @Override
    public String toString() {
        return inner.toString();
    }

    @Override
    public Input read(Parser outer, TokenStream input, boolean all) throws ParsingException {
        if (!input.hasAtLeast(limit))
            throw new ParsingException("Not enough tokens remaining for '" + this + "': " + input.remaining());
        return inner.read(outer, input, all);
    }

    @Override
    public CharSequence[] elements() {
        return inner.elements();
    }

}
