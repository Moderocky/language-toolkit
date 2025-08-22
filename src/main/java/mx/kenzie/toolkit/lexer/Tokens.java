package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.lexer.stream.ArrayTokenStream;
import mx.kenzie.toolkit.lexer.stream.FixedTokenList;
import mx.kenzie.toolkit.lexer.stream.TokenList;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.lexer.token.StructureToken;
import mx.kenzie.toolkit.lexer.token.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Tokens extends Iterable<Token> {

    static Tokens threadSafe() {
        return new TokenList();
    }

    static Tokens simple() {
        return new FixedTokenList();
    }

    static Tokens from(Collection<Token> tokens) {
        return new FixedTokenList(tokens);
    }

    void ignoreWhitespace();

    boolean add(Token token);

    boolean isEmpty();

    Token first();

    Token last();

    default TokenStream forParsing() {
        return new ArrayTokenStream(this);
    }

    default boolean hasMatchingBrackets() {
        int round = 0, square = 0, curly = 0;
        for (Token token : this) {
            if (!(token instanceof StructureToken structure)) continue;
            switch (structure.symbol()) {
                case '(' -> ++round;
                case '[' -> ++square;
                case '{' -> ++curly;
                case ')' -> --round;
                case ']' -> --square;
                case '}' -> --curly;
            }
        }
        return round == 0 && square == 0 && curly == 0;
    }

    default Token[] toArray() {
        List<Token> list = new ArrayList<>();
        for (Token token : this) {
            list.add(token);
        }
        return list.toArray(new Token[0]);
    }

    default void addAll(Tokens tokens) {
        for (Token token : tokens) {
            this.add(token);
        }
    }

    default int size() {
        int count = 0;
        for (Token _ : this) ++count;
        return count;
    }

}

