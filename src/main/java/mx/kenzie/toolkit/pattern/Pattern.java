package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.PatternException;

public record Pattern(CharSequence... elements) implements CharSequence, Elements, SubParser {

    public static final CharSequence WORD = new Word(), NUMBER = new Number(), TEXT = new Text(), NAME = new Identifier();

    public static Pattern pattern(CharSequence... elements) {
        return new Pattern(elements);
    }

    public static CharSequence round(CharSequence... elements) {
        return brackets('(', ')', elements);
    }

    public static CharSequence curly(CharSequence... elements) {
        return brackets('{', '}', elements);
    }

    public static CharSequence square(CharSequence... elements) {
        return brackets('[', ']', elements);
    }

    public static CharSequence brackets(char open, char close, CharSequence... elements) {
        return new Brackets(open, close, elements);
    }

    public static CharSequence repeat(CharSequence... elements) {
        if (elements.length == 0)
            throw new PatternException("No repeatable unit.");
        return new Repeater(elements);
    }

    public static CharSequence csv(CharSequence... elements) {
        return separated(",", elements);
    }

    public static CharSequence separated(String separator, CharSequence... elements) {
        if (elements.length == 0)
            throw new PatternException("No repeatable unit.");
        return new CSVRepeater(separator, elements);
    }

    public LeftRecursive leftRecursive(Assembler assembler) {
        if (elements.length < 2)
            throw new PatternException("Left-recursive patterns must have 2 or more elements.");
        CharSequence first = elements[0];
        CharSequence[] rest = new CharSequence[elements.length - 1];
        System.arraycopy(elements, 1, rest, 0, elements.length - 1);
        return new LeftRecursive(assembler, first, rest);
    }

    public CharSequence leftRecursive(int limit, Assembler assembler) {
        return new Limited<>(this.leftRecursive(assembler), limit);
    }

    @Override
    public String toString() {
        return String.join(" ", elements);
    }

}