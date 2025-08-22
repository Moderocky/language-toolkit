package mx.kenzie.toolkit.parser;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.lexer.stream.TokenStream;
import mx.kenzie.toolkit.model.Model;

import java.util.function.Consumer;

record Result(Model model, ParsingException error, Consumer<TokenStream> forkPoint) {

    public static Result of(Model model, Consumer<TokenStream> forkPoint) {
        return new Result(model, null, forkPoint);
    }

    public static Result of(ParsingException error) {
        return new Result(null, error, null);
    }

    public boolean success() {
        return error == null;
    }

}
