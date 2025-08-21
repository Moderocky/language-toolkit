package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.ModelTest;
import mx.kenzie.toolkit.error.ParsingException;
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
//
//    public static void main(String[] args) throws ParsingException {
//        StreamUnitTest streamUnitTest = new StreamUnitTest();
//        streamUnitTest.setUp();
//        Model parse = streamUnitTest.grammar.parseLive(streamUnitTest.outer, System.in);
//        System.out.println(parse);
//
//    }

    @Test
    public void register() throws ParsingException {
        Model parse = grammar.parseLive(outer, new StringReader("class foo { bar ; }"));
        System.out.println(parse);
    }

    @Test
    public void parse() {
    }

    private record Outer(String word, Inner... inners) implements ModelTest.SimpleModel {

    }

    private record Inner() implements ModelTest.SimpleModel {

    }

}