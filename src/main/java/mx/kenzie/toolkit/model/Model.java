package mx.kenzie.toolkit.model;

import mx.kenzie.toolkit.lexer.Position;

import java.io.PrintStream;

public interface Model {

    void print(PrintStream stream);

    default Position position() {
        return Position.NOWHERE;
    }

}
