package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Parser;
import mx.kenzie.toolkit.parser.Unit;

import java.util.*;

public class Grammar implements Parser {

    final Map<Unit, Collection<PatternParser>> parsers;

    public Grammar() {
        this(new LinkedHashMap<>());
    }

    public Grammar(Grammar grammar) {
        this(new LinkedHashMap<>(grammar.parsers));
    }

    Grammar(Map<Unit, Collection<PatternParser>> parsers) {
        this.parsers = parsers;
    }

    public void register(Unit unit, Pattern pattern, Assembler assembler) {
        final var parsers = this.parsers.computeIfAbsent(unit, _ -> new ArrayList<>());
        parsers.add(new PatternParser(pattern, assembler));
    }

    @Override
    public Model parse(Parser parser, TokenStream input, boolean all) throws ParsingException {
        throw new ParsingException("Grammar has no base-level syntax.");
    }

    @Override
    public Iterable<Parser> parsers(Parser outer, Unit unit) {
        //noinspection rawtypes,unchecked
        return (Iterable<Parser>) (Collection) parsers.getOrDefault(unit, Collections.emptyList());
    }

}
