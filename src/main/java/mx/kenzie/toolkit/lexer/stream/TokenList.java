package mx.kenzie.toolkit.lexer.stream;

import mx.kenzie.toolkit.lexer.Tokens;
import mx.kenzie.toolkit.lexer.token.Token;
import mx.kenzie.toolkit.lexer.token.WhitespaceToken;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class TokenList implements Tokens {

    protected transient final ReentrantLock lock = new ReentrantLock(false);
    final Block pending = new Block();
    protected volatile TokenList.Node first, last;
    protected volatile boolean done;
    protected boolean ignoreWhitespace;

    @Override
    public void ignoreWhitespace() {
        this.ignoreWhitespace = true;
        if (first == null) return;
        lock.lock();
        try {
            int size = last.estimateIndex;
            TokenList.Node node = first;
            while (node != null) {
                if (node.token instanceof WhitespaceToken) {
                    node.unlink();
                    --size;
                }
                node = node.next;
            }
            last.estimateIndex = size;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(Token token) {
        if (token instanceof WhitespaceToken && ignoreWhitespace)
            return false;
        lock.lock();
        try {
            if (first == null)
                this.first = last = new Node(token);
            else {
                this.last = last.next(token);
            }
            pending.complete();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        if (first != null) return false;
        lock.lock();
        try {
            return first == null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Token first() {
        if (first != null)
            return first.token;
        lock.lock();
        try {
            if (first == null)
                throw new NoSuchElementException();
            return first.token;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Token last() {
        lock.lock();
        try {
            if (last == null)
                throw new NoSuchElementException();
            return last.token;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return last == null ? 0 : last.estimateIndex;
        } finally {
            lock.unlock();
        }
    }

    public void markDone() {
        lock.lock();
        try {
            done = true;
            pending.complete();
        } finally {
            lock.unlock();
        }
    }

    public Node awaitFirstNode() {
        if (first != null) return first;
        lock.lock();
        try {
            if (done)
                return first;
        } finally {
            lock.unlock();
        }
        pending.await();
        return first;
    }

    public void poll() {
        lock.lock();
        try {
            if (done) return;
        } finally {
            lock.unlock();
        }
        pending.reset();
        pending.await();
    }

    public TokenList from(TokenList.Node node) {
        TokenList list = new TokenList();
        lock.lock();
        list.first = node;
        try {
            list.last = last;
            return list;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<Token> iterator() {
        return new Iterator<>() {
            TokenList.Node current = first;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Token next() {
                try {
                    return current.token;
                } finally {
                    current = current.next();
                }
            }
        };
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (Token token : this) {
            result = 31 * result + Objects.hashCode(token);
        }
        return result;
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

    public class Node {

        protected final Token token;
        protected volatile TokenList.Node previous;
        protected volatile TokenList.Node next;
        protected volatile int estimateIndex = 1;

        protected Node(TokenList.Node previous, Token token, TokenList.Node next) {
            this.previous = previous;
            this.token = token;
            this.next = next;
        }

        protected Node(TokenList.Node previous, Token token) {
            this(previous, token, null);
        }

        protected Node(Token token) {
            this(null, token, null);
        }

        public Node next() {
            if (next != null) // Assume we can already see the change from here
                return next;
            lock.lock();
            try {
                if (next != null)
                    return next;
                if (done)
                    return null;
                pending.reset();
            } finally {
                lock.unlock();
            }
            pending.await();
            lock.lock();
            try {
                return next;
            } finally {
                lock.unlock();
            }
        }

        public Node previous() {
            return previous;
        }

        public TokenList.Node next(Token token) {
            lock.lock();
            try {
                TokenList.Node node = new TokenList.Node(this, token);
                this.next = node;
                node.estimateIndex = estimateIndex + 1;
                pending.complete();
                return node;
            } finally {
                lock.unlock();
            }
        }

        public void unlink() {
            lock.lock();
            try {
                if (previous != null) {
                    previous.next = next;
                }
                if (next != null) next.previous = previous;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TokenList.Node) obj;
            return Objects.equals(this.token, that.token);
        }

        @Override
        public String toString() {
            return Objects.toString(token);
        }

        public int following() {
            lock.lock();
            try {
                return last.estimateIndex - this.estimateIndex;
            } finally {
                lock.unlock();
            }
        }

    }

}
