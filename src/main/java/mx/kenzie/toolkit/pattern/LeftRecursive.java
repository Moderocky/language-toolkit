package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.error.PatternException;
import mx.kenzie.toolkit.lexer.Mark;
import mx.kenzie.toolkit.lexer.Position;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Parser;

record LeftRecursive(Assembler assembler, CharSequence recursive,
                     CharSequence... elements) implements Elements, SubParser {

    LeftRecursive {
        if (elements.length < 1) throw new PatternException("Not enough pattern elements for left recursion.");
    }

    @Override
    public String toString() {
        return recursive + " " + String.join(" ", elements);
    }

    @Override
    public Input read(Parser outer, TokenStream input, boolean all) throws ParsingException {
        Position position = input.here();
        Model model = this.parseFirst(outer, input);
        do try (Mark mark = input.markForReset()) {
            Input read = SubParser.super.read(outer, input, false);
            Input included = read.prepend(position, model);
            model = assembler.apply(included);
            mark.discard();
        } catch (ParsingException ex) {
            break;
        } while (input.hasNext());
        if (all && input.hasNext())
            throw new ParsingException("Too many tokens remaining for '" + this + "': " + input.remaining());
        return new Input(position, model);
    }

    private Model parseFirst(Parser outer, TokenStream input) throws ParsingException {
        CharSequence[] sub = new CharSequence[elements.length + 1];
        sub[0] = recursive;
        System.arraycopy(elements, 0, sub, 1, elements.length);
        Input read = this.read(sub, outer, input, false);
        return assembler.apply(read);
    }

}
