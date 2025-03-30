package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.Position;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Unit;
import org.junit.Test;

import java.io.PrintStream;

import static mx.kenzie.toolkit.pattern.Pattern.pattern;

public class GrammarTest {

    @Test
    public void register() throws ParsingException {
        final Unit a = () -> "a", b = () -> "b";
        record Dummy(Position position, Model... models) implements SimpleModel {

        }

        Grammar grammar = new Grammar();
        grammar.register(a, pattern("a", b),
            inputs -> new Dummy(inputs.position(), inputs.next(Model.class)));
        grammar.register(b, pattern("b"), inputs -> new Dummy(inputs.position()));
        Model parsed = grammar.parse(a, "a b");
        assert parsed instanceof Dummy dummy
            && dummy.models.length == 1
            && dummy.models[0] instanceof Dummy inner
            && inner.models.length == 0;
    }

    @Test
    public void parse() {
    }

    interface SimpleModel extends Model {

        @Override
        default void print(PrintStream stream) {
            stream.print(this);
        }

    }

}