package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.model.Model;

@FunctionalInterface
public interface Assembler {

    Model apply(Inputs inputs) throws ParsingException;

}
