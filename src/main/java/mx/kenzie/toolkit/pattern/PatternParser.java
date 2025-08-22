package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Parser;

record PatternParser(Pattern pattern, Assembler assembler) implements Parser, Elements, SubParser {

    @Override
    public Model parse(Parser outer, TokenStream input, boolean all) throws ParsingException {
        Input inputs = pattern.read(outer, input, all);
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
    public Input read(Parser outer, TokenStream input, boolean all) throws ParsingException {
        Input read = SubParser.super.read(outer, input, all);
        return new Input(read.position, assembler.apply(read));
    }

}
