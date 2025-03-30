package mx.kenzie.toolkit.lexer;

import junit.framework.TestCase;
import mx.kenzie.toolkit.lexer.token.WordToken;
import org.junit.Test;

import java.io.StringReader;

public class LexerTest extends TestCase {

    @Test
    public void testLexer() throws Throwable {
        TokenList test = new Lexer(new StringReader("test")).run();
        assert test.size() == 1;
        assert test.getFirst() instanceof WordToken;
    }

}