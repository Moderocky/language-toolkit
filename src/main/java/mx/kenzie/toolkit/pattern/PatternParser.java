package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.CallStack;
import mx.kenzie.toolkit.parser.Parser;

record PatternParser(Pattern pattern, Assembler assembler) implements Parser, Elements, SubParser {

    @Override
    public CallStack callStack(Parser outer) {
        return outer.callStack(this);
    }

    @Override
    public void updateCallStack(Parser outer, CallStack stack) {
        outer.updateCallStack(this, stack);
    }

    @Override
    public Model parse(Parser outer, TokenStream input, boolean all) throws ParsingException {
        CallStack stack = outer.callStack(outer);
        if (stack.peek() == this)
            throw new ParsingException("Cannot parse '" + pattern + "' inside itself.");
        outer.updateCallStack(outer, stack.push(this));
        try {
            Input inputs = pattern.read(outer, input, all);
            return assembler.apply(inputs);
        } finally {
            outer.updateCallStack(outer, outer.callStack(outer).pop());
        }
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
