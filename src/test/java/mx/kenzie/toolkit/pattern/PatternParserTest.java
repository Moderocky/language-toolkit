package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Unit;
import org.junit.Test;

import java.io.PrintStream;
import java.io.StringReader;

import static mx.kenzie.toolkit.pattern.Pattern.pattern;

public class PatternParserTest {

    @Test(expected = ParsingException.class)
    public void infiniteRecursion() throws ParsingException {
        Unit unit = Unit.of("thing");
        Grammar grammar = new Grammar();
        grammar.register(unit, pattern(unit, "blob"), _ -> null);
        StringReader source = new StringReader("blob blob blob");
        Model parsed = grammar.parse(unit, source);
        assert parsed != null;
    }

    @Test
    public void noRecursion() throws ParsingException {
        Unit unit = Unit.of("thing");
        Grammar grammar = new Grammar();
        grammar.register(unit, pattern("blob"), _ -> PrintStream::println);
        StringReader source = new StringReader("blob");
        Model parsed = grammar.parse(unit, source);
        assert parsed != null;
    }

    @Test
    public void avoidRecursion() throws ParsingException {
        Unit unit = Unit.of("thing");
        Grammar grammar = new Grammar();
        grammar.register(unit, pattern("blob"), _ -> PrintStream::println);
        grammar.register(unit, pattern(unit, "blob"), _ -> PrintStream::println);
        StringReader source = new StringReader("blob");
        Model parsed = grammar.parse(unit, source);
        assert parsed != null;
    }

    @Test
    public void avoidRecursionOtherWay() throws ParsingException {
        Unit unit = Unit.of("thing");
        Grammar grammar = new Grammar();
        grammar.register(unit, pattern(unit, "blob"), _ -> PrintStream::println);
        grammar.register(unit, pattern("blob"), _ -> PrintStream::println);
        StringReader source = new StringReader("blob");
        Model parsed = grammar.parse(unit, source);
        assert parsed != null;
    }

}