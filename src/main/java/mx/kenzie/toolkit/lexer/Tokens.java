package mx.kenzie.toolkit.lexer;

import mx.kenzie.toolkit.lexer.token.StructureToken;
import mx.kenzie.toolkit.lexer.token.Token;
import mx.kenzie.toolkit.lexer.token.WhitespaceToken;

import java.util.*;

public interface Tokens extends Iterable<Token> {

    static Tokens empty() {
        return new TokenList();
    }

    static Tokens from(Collection<Token> tokens) {
        return new FixedTokenList(tokens);
    }

    void removeWhitespace();

    boolean add(Token token);

    boolean isEmpty();

    Token first();

    Token last();

    default TokenStream forParsing() {
        return new KnownTokenStream(this);
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
        for (Token token : this) ++count;
        return count;
    }

}

class TokenList implements Tokens {

    protected volatile Node first, last;

    protected boolean shouldRemoveWhitespace;

    @Override
    public synchronized void removeWhitespace() {
        this.shouldRemoveWhitespace = true;
        Node node = first;
        while (node != null) {
            if (node.token instanceof WhitespaceToken) {
                node.unlink();
                --last.estimateIndex;
            }
            node = node.next;
        }
    }

    @Override
    public synchronized boolean add(Token token) {
        if (token instanceof WhitespaceToken && shouldRemoveWhitespace)
            return false;
        if (first == null)
            this.first = last = new Node(token);
        else
            this.last = last.next(token);
        return true;
    }

    public boolean isEmpty() {
        return first == null;
    }

    @Override
    public Token first() {
        if (first == null) throw new NoSuchElementException();
        return first.token;
    }

    @Override
    public synchronized Token last() {
        if (last == null) throw new NoSuchElementException();
        return last.token;
    }

    @Override
    public int size() {
        return last == null ? 0 : last.estimateIndex;
    }

    synchronized TokenList from(Node node) {
        TokenList list = new TokenList();
        list.first = node;
        list.last = last;
        return list;
    }

    TokenList to(Node node) {
        TokenList list = new TokenList();
        list.first = first;
        list.last = node;
        return list;
    }

    @Override
    public Iterator<Token> iterator() {
        return new Iterator<>() {
            Node current = first;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Token next() {
                try {
                    return current.token;
                } finally {
                    current = current.next;
                }
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean first = true;
        for (Token token : this) {
            if (first) first = false;
            else builder.append(", ");
            builder.append(token);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Iterable<?> tokens)) return false;
        Iterator<?> it = tokens.iterator();
        Iterator<?> it2 = this.iterator();
        while (it.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it.next(), it2.next())) return false;
        }
        return (it.hasNext() == it2.hasNext());
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (Token token : this) {
            result = 31 * result + Objects.hashCode(token);
        }
        return result;
    }

    static class Node {

        protected final Token token;
        protected volatile Node previous;
        protected volatile Node next;
        protected volatile int estimateIndex = 1;

        Node(Node previous, Token token, Node next) {
            this.previous = previous;
            this.token = token;
            this.next = next;
        }

        public Node(Node previous, Token token) {
            this(previous, token, null);
        }

        public Node(Token token) {
            this(null, token, null);
        }

        public Node next(Token token) {
            Node node = new Node(this, token);
            this.next = node;
            node.estimateIndex = estimateIndex + 1;
            return node;
        }

        public void unlink() {
            if (previous != null) {
                previous.next = next;
                previous.estimateIndex = estimateIndex - 1;
            }
            if (next != null) next.previous = previous;
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Node) obj;
            return Objects.equals(this.token, that.token);
        }

        @Override
        public String toString() {
            return Objects.toString(token);
        }

        public int following() {
            int count = 0;
            Node current = next;
            while (current != null) {
                ++count;
                current = current.next;
            }
            return count;
        }

    }

}

class FixedTokenList extends ArrayList<Token> implements Tokens {

    public FixedTokenList() {
    }

    public FixedTokenList(Collection<? extends Token> c) {
        super(c);
    }

    @Override
    public void removeWhitespace() {
        this.removeIf(token -> token instanceof WhitespaceToken);
    }

    @Override
    public Token first() {
        return this.getFirst();
    }

    @Override
    public Token last() {
        return this.getLast();
    }

    @Override
    public Token[] toArray() {
        return this.toArray(new Token[0]);
    }

}
