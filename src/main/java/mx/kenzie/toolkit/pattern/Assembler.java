package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.model.Model;

@FunctionalInterface
public interface Assembler {

    Model apply(Input input) throws ParsingException;

    static Assembler name(String name, Assembler assembler) {
        //<editor-fold desc="Named assembler wrapper" defaultstate="collapsed">
        class NamedAssembler implements Assembler {

            @Override
            public Model apply(Input input) throws ParsingException {
                return assembler.apply(input);
            }

            @Override
            public String toString() {
                return name;
            }

        }
        return new NamedAssembler();
        //</editor-fold>
    }

}
