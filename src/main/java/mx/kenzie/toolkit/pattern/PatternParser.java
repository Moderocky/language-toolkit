package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Parser;

record PatternParser(Pattern pattern, Assembler assembler) implements Parser {

    @Override
    public Model parse(Parser outer, TokenStream input, boolean all) throws ParsingException {
        Inputs inputs = pattern.read(outer, input, all);
        return assembler.apply(inputs);
    }

}
