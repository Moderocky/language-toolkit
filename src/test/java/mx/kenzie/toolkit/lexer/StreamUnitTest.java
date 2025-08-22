package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.ModelTest;
import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Unit;
import mx.kenzie.toolkit.pattern.Grammar;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static mx.kenzie.toolkit.pattern.Pattern.*;

public class StreamUnitTest {

    final Unit outer = Unit.of("outer"), inner = Unit.of("inner");
    final Grammar grammar = new Grammar();

    @Before
    public void setUp() {
        grammar.register(outer, pattern("class", WORD, curly(inner)), input ->
            new Outer(input.next(String.class), input.consumeAll(Inner.class, true)));
        grammar.register(outer, pattern("class", WORD, "blah", curly(inner)), input ->
            new Outer(input.next(String.class), input.consumeAll(Inner.class, true)));
        grammar.register(inner, pattern(WORD, ";"), _ -> new Inner());
    }

    @Test
    public void parse1() throws ParsingException {
        Model parse = grammar.parseLive(outer, new StringReader("class foo { bar ; }"));
        assert parse != null;
    }

    @Test
    public void parse2() throws ParsingException {
        StringReader source = new StringReader("class foo { bar ; } class foo { bar ; }");
        Lexer lexer = new Lexer(source);
        TokenStream live = lexer.live();
        Model first = grammar.parse(grammar, outer, live, false);
        Model second = grammar.parse(grammar, outer, live, false);
        assert first != null;
        assert second != null;
    }

    @Test
    public void parseInfinite() throws ParsingException {
        Grammar grammar = new Grammar();
        grammar.register(outer, pattern(outer, "blob"), _ -> new Inner());
        grammar.register(outer, pattern("blob"), _ -> new Inner());
        StringReader source = new StringReader("blob");
        Lexer lexer = new Lexer(source);
        TokenStream live = lexer.live();
        Model first = grammar.parse(grammar, outer, live, true);
        assert first != null;
    }

    private record Outer(String word, Inner... inners) implements ModelTest.SimpleModel {

    }

    private record Inner() implements ModelTest.SimpleModel {

    }

}