package mx.kenzie.toolkit.lexer;

import junit.framework.TestCase;
import mx.kenzie.toolkit.lexer.token.NumberToken;
import mx.kenzie.toolkit.lexer.token.Token;
import mx.kenzie.toolkit.lexer.token.WordLikeToken;
import mx.kenzie.toolkit.lexer.token.WordToken;
import org.junit.Test;

import java.io.StringReader;
import java.util.Iterator;

public class LexerTest extends TestCase {

    @Test
    public void testLexer() throws Throwable {
        Tokens test = new Lexer(new StringReader("test")).run();
        assert test.size() == 1;
        assert test.first() instanceof WordToken;
    }

    @Test
    @SuppressWarnings("AssertWithSideEffects")
    public void testSeparation() throws Throwable {
        Tokens test = new Lexer(new StringReader("test test1 1 -3 5++5 --27 -foo +foo")).run();
        test.removeWhitespace();
        Iterator<Token> iterator = test.iterator();
        assert iterator.next() instanceof WordToken word
            && word.value().equals("test");
        assert iterator.next() instanceof WordToken word
            && word.value().equals("test1");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(1);
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("-");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(3);
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(5);
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("++");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(5);
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("--");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(27);
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("-");
        assert iterator.next() instanceof WordToken word
            && word.value().equals("foo");
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("+");
        assert iterator.next() instanceof WordToken word
            && word.value().equals("foo");
        assert !iterator.hasNext();
    }

    @Test
    @SuppressWarnings("AssertWithSideEffects")
    public void testSymbols() throws Throwable {
        Tokens test = new Lexer(new StringReader("test-test foo-1 --1 foo+2 1-1")).run();
        test.removeWhitespace();
        Iterator<Token> iterator = test.iterator();
        assert iterator.next() instanceof WordToken word
            && word.value().equals("test");
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("-");
        assert iterator.next() instanceof WordToken word
            && word.value().equals("test");
        assert iterator.next() instanceof WordToken word
            && word.value().equals("foo");
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("-");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(1);
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("--");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(1);
        assert iterator.next() instanceof WordToken word
            && word.value().equals("foo");
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("+");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(2);
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(1);
        assert iterator.next() instanceof WordLikeToken word
            && word.value().equals("-");
        assert iterator.next() instanceof NumberToken number
            && number.value().equals(1);
        assert !iterator.hasNext();
    }

}