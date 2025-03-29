package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Unit;
import org.junit.Test;

import static mx.kenzie.toolkit.pattern.Pattern.pattern;

public class GrammarTest {

    private final Unit a = () -> "a", b = () -> "b";

    @Test
    public void register() {
        Grammar grammar = new Grammar();
        grammar.register(a, pattern(""),
            inputs -> new Model() {
            });
    }

    @Test
    public void parse() {
    }

}