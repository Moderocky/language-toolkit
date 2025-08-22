package mx.kenzie.toolkit.parser;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.Tokens;
import mx.kenzie.toolkit.lexer.stream.Mark;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.lexer.token.Token;
import mx.kenzie.toolkit.lexer.token.WordLikeToken;
import mx.kenzie.toolkit.model.Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public record PatternMatcher(List<Object> elements) {

    public static PatternMatcher make() {
        return new PatternMatcher(new ArrayList<>());
    }

    public PatternMatcher word(String literal) {
        this.elements.add(literal);
        return this;
    }

    public PatternMatcher expect(Unit unit) {
        this.elements.add(unit);
        return this;
    }

    public Model[] parse(Parser outer, TokenStream input, boolean all) throws ParsingException {
        ArrayIterator iterator = new ArrayIterator(elements.toArray());
        List<Model> models = new ArrayList<>();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (!input.hasNext()) throw new ParsingException("Reached end of input while expecting " + element);
            try (Mark mark = input.markForReset()) {
                switch (element) {
                    case String literal:
                        Token next = input.next();
                        if (next instanceof WordLikeToken word && Objects.equals(word.value(), literal)) mark.discard();
                        else throw new ParsingException("Expected '" + literal + "' but found " + next);
                        break;
                    case Unit unit:
                        String token = this.nextSolidToken(iterator);
                        if (token == null) {
                            models.add(outer.parse(outer, unit, input, all));
                        } else {
                            Tokens list = Tokens.simple();
                            List<ParsingException> errors = new ArrayList<>();
                            boolean multiple = false, hasParsed = false;
                            for (Token thing : input) {
                                if (thing instanceof WordLikeToken word && Objects.equals(word.value(), token)) {
                                    TokenStream stream = list.forParsing();
                                    try {
                                        while (stream.hasNext())
                                            models.add(outer.parse(outer, unit, stream, !multiple));
                                        hasParsed = true;
                                        break;
                                    } catch (ParsingException error) {
                                        errors.add(error);
                                    }
                                }
                                list.add(thing);
                            }
                            if (!hasParsed) {
                                ParsingException exception = new ParsingException("Unable to find a suitable " + unit);
                                errors.forEach(exception::addSuppressed);
                                throw exception;
                            }
                        }
                        mark.discard();
                        break;
                    case null, default:
                        throw new ParsingException("Unexpected element: " + element);
                }
            }
        }
        return models.toArray(new Model[0]);
    }

    private String nextSolidToken(ArrayIterator iterator) {
        ArrayIterator clone = iterator.clone();
        while (clone.hasNext()) {
            Object element = clone.next();
            if (element instanceof String literal) return literal;
        }
        return null;
    }

    private static class ArrayIterator implements Iterator<Object> {

        private final Object[] elements;
        private int index = 0;

        ArrayIterator(Object[] elements) {
            this.elements = elements;
        }

        public ArrayIterator(Object[] elements, int index) {
            this.elements = elements;
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < elements.length;
        }

        @Override
        public Object next() {
            return elements[index++];
        }

        @Override
        protected ArrayIterator clone() {
            return new ArrayIterator(elements, index);
        }

    }


}
