package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.Lexer;
import mx.kenzie.toolkit.lexer.TokenList;
import mx.kenzie.toolkit.lexer.TokenStream;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static mx.kenzie.toolkit.pattern.Pattern.*;

public class PatternTest {

    protected TokenStream sample(String string) {
        Lexer lexer = new Lexer(new StringReader(string));
        TokenList list;
        try {
            list = lexer.run();
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
        list.removeWhitespace();
        return new TokenStream(list);
    }

    protected Inputs test(Pattern pattern, String string) {
        try {
            PatternParser parser = new PatternParser(pattern, _ -> null);
            return pattern.read(parser, sample(string), true);
        } catch (ParsingException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void parseTestCSV() {
        Pattern pattern = pattern(csv(WORD));
        Inputs inputs = this.test(pattern, "hello, there");
        assert inputs.is(Inputs.class);
        Inputs repeater = inputs.next(Inputs.class);
        Inputs first = repeater.next(Inputs.class);
        assert first.hasNext();
        assert first.next(String.class).equals("hello");
        assert !first.hasNext();
        Inputs second = repeater.next(Inputs.class);
        assert second.hasNext();
        assert second.next(String.class).equals("there");
        assert !second.hasNext();
        assert !repeater.hasNext();
        assert !inputs.hasNext();
    }

    @Test(expected = AssertionError.class)
    public void parseTestCSVError() {
        Pattern pattern = pattern(csv(WORD));
        this.test(pattern, "hello there");
    }

    @Test
    public void parseTestRepeating() {
        Pattern pattern = pattern(repeat(WORD));
        Inputs inputs = this.test(pattern, "hello there");
        assert inputs.is(Inputs.class);
        Inputs repeater = inputs.next(Inputs.class);
        Inputs first = repeater.next(Inputs.class);
        assert first.hasNext();
        assert first.next(String.class).equals("hello");
        assert !first.hasNext();
        Inputs second = repeater.next(Inputs.class);
        assert second.hasNext();
        assert second.next(String.class).equals("there");
        assert !second.hasNext();
        assert !repeater.hasNext();
        assert !inputs.hasNext();
    }

    @Test
    public void parseTestWordRepeating() {
        Pattern pattern = pattern("hello", repeat(WORD));
        Inputs inputs = this.test(pattern, "hello there");
        assert inputs.is(Inputs.class);
        Inputs repeater = inputs.next(Inputs.class);
        Inputs first = repeater.next(Inputs.class);
        assert first.hasNext();
        assert first.next(String.class).equals("there");
        assert !first.hasNext();
        assert !repeater.hasNext();
        assert !inputs.hasNext();
    }

    @Test
    public void parseTestWordRepeatingLiteral() {
        Pattern pattern = pattern("hello", repeat("there"));
        Inputs inputs = this.test(pattern, "hello there");
        assert inputs.is(Inputs.class);
        Inputs repeater = inputs.next(Inputs.class);
        Inputs first = repeater.next(Inputs.class);
        assert !first.hasNext();
        assert !repeater.hasNext();
        assert !inputs.hasNext();
    }

    @Test
    public void parseTestWordRepeatingLiteralTwice() {
        Pattern pattern = pattern("hello", repeat("there"));
        Inputs inputs = this.test(pattern, "hello there there");
        assert inputs.is(Inputs.class);
        Inputs repeater = inputs.next(Inputs.class);
        assert repeater.size() == 2;
        Inputs first = repeater.next(Inputs.class);
        assert !first.hasNext();
        Inputs second = repeater.next(Inputs.class);
        assert !second.hasNext();
        assert !repeater.hasNext();
        assert !inputs.hasNext();
    }

    @Test
    public void parseTestTwoWordInput() {
        Pattern pattern = pattern(WORD, WORD);
        Inputs inputs = this.test(pattern, "hello there");
        assert inputs.hasNext();
        assert inputs.next(String.class).equals("hello");
        assert inputs.hasNext();
        assert inputs.next(String.class).equals("there");
        assert !inputs.hasNext();
    }

    @Test(expected = AssertionError.class)
    public void parseTestTwoWordInputLong() {
        Pattern pattern = pattern(WORD, WORD);
        this.test(pattern, "hello there world");
    }

    @Test(expected = AssertionError.class)
    public void parseTestTwoWordInputShort() {
        Pattern pattern = pattern(WORD, WORD);
        this.test(pattern, "hello");
    }

    @Test
    public void parseTestWordInput() {
        Pattern pattern = pattern("hello", WORD);
        Inputs inputs = this.test(pattern, "hello there");
        assert inputs.hasNext();
        assert inputs.next(String.class).equals("there");
        assert !inputs.hasNext();
    }

    @Test(expected = AssertionError.class)
    public void parseTestWordInputNothing() {
        Pattern pattern = pattern("hello", WORD);
        this.test(pattern, "hello    ");
    }

    @Test(expected = AssertionError.class)
    public void parseTestWordInputLong() {
        Pattern pattern = pattern("hello", WORD);
        this.test(pattern, "hello there there");
    }

    @Test
    public void parseTestWordInputWord() {
        Pattern pattern = pattern("hello", WORD, "there");
        Inputs inputs = this.test(pattern, "hello world there");
        assert inputs.hasNext();
        assert inputs.next(String.class).equals("world");
        assert !inputs.hasNext();
    }

    @Test(expected = AssertionError.class)
    public void parseTestWordInputWordShort() {
        Pattern pattern = pattern("hello", WORD, "there");
        this.test(pattern, "hello there");
    }

    @Test
    public void parseTestSimple() {
        Pattern pattern = pattern("hello", "there");
        Inputs inputs = this.test(pattern, "hello there");
        assert !inputs.hasNext();
    }

    @Test(expected = AssertionError.class)
    public void parseTestSimpleFailureShort() {
        Pattern pattern = pattern("hello", "there");
        this.test(pattern, "hello");
    }

    @Test(expected = AssertionError.class)
    public void parseTestSimpleFailureLong() {
        Pattern pattern = pattern("hello", "there");
        this.test(pattern, "hello there there");
    }

    @Test(expected = AssertionError.class)
    public void parseTestSimpleFailureWrongWord() {
        Pattern pattern = pattern("hello", "there");
        this.test(pattern, "hello world");
    }

    @Test
    public void patternTest() {
        Pattern pattern = pattern("hello", "there");
        assert pattern.toString().equals("hello there");
        assert pattern.elements().length == 2;
        assert pattern.elements()[0].equals("hello");
        assert pattern.elements()[1].equals("there");
    }

    @Test
    public void roundTest() {
        Pattern pattern = pattern(round(WORD, WORD));
        assert pattern.toString().equals("(<word> <word>)") : pattern.toString();
        assert pattern.elements().length == 1;
    }

    @Test
    public void curlyTest() {
        Pattern pattern = pattern(curly(WORD, WORD));
        assert pattern.toString().equals("{<word> <word>}") : pattern.toString();
        assert pattern.elements().length == 1;
    }

    @Test
    public void squareTest() {
        Pattern pattern = pattern(square(WORD, WORD));
        assert pattern.toString().equals("[<word> <word>]") : pattern.toString();
        assert pattern.elements().length == 1;
    }

    @Test
    public void bracketsTest() {
        Pattern pattern = pattern(brackets('^', '$', WORD, WORD));
        assert pattern.toString().equals("^<word> <word>$") : pattern.toString();
        assert pattern.elements().length == 1;
    }

    @Test
    public void repeatTest() {
        Pattern pattern = pattern(repeat(WORD));
        assert pattern.toString().equals("<word>...") : pattern.toString();
        assert pattern.elements().length == 1;
    }

    @Test
    public void csvTest() {
        Pattern pattern = pattern(csv(WORD));
        assert pattern.toString().equals("<word>, ...") : pattern.toString();
        assert pattern.elements().length == 1;
    }

    @Test
    public void testToString() {
    }

}