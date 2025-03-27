package mx.kenzie.toolkit.parser;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.Mark;
import mx.kenzie.toolkit.lexer.TokenList;
import mx.kenzie.toolkit.lexer.TokenStream;
import mx.kenzie.toolkit.lexer.token.StructureToken;
import mx.kenzie.toolkit.model.Model;

import java.util.ArrayList;
import java.util.List;

public interface CSVParser extends Parser {

    default Model[] parseCSVs(Parser outer, TokenList list, Unit unit) throws ParsingException {
        final TokenStream stream = new TokenStream(list);
        final List<Model> models = new ArrayList<>();
        outer:
        while (stream.hasNext()) {
            TokenList until = new TokenList();
            do {
                try (Mark mark = stream.markForReset()) {
                    until.addAll(this.getEverythingUntil(StructureToken.class, stream, token -> token.symbol() == ','));
                    try {
                        final Model parsed = this.parse(outer, unit, new TokenStream(until), true);
                        mark.discard();
                        models.add(parsed);
                        continue outer;
                    } catch (ParsingException e) {
                        until = this.take(stream, until.size() + 1);
                    }
                }
            } while (stream.hasNext());
        }
        return models.toArray(new Model[0]);
    }

}
