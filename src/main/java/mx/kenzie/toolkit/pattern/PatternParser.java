package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Parser;

record PatternParser(Pattern pattern, Assembler assembler) implements Parser, Elements, SubParser {

    @Override
    public Model parse(Parser outer, TokenStream input, boolean all) throws ParsingException {
        Inputs inputs = pattern.read(outer, input, all);
        return assembler.apply(inputs);
    }

    @Override
    public CharSequence[] elements() {
        return pattern.elements();
    }

    @Override
    public String toString() {
        return pattern.toString();
    }

    @Override
    public Inputs read(Parser outer, TokenStream input, boolean all) throws ParsingException {
        Inputs read = SubParser.super.read(outer, input, all);
        return new Inputs(read.position, assembler.apply(read));
    }

}
