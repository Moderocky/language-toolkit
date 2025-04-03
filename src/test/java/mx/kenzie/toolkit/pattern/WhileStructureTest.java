package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.ModelTest;
import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Unit;
import org.junit.Test;

import static mx.kenzie.toolkit.pattern.Pattern.*;

public class WhileStructureTest {

    final Unit root = () -> "root", statement = () -> "statement",
        expression = () -> "expression", variable = () -> "variable";

    @Test
    public void whileGrammar() throws ParsingException {
        Grammar grammar = new Grammar();
        grammar.register(root, pattern(WORD, "read", variable, statement, "write", variable),
            inputs -> new Program(inputs.next(String.class),
                inputs.next(Variable.class),
                inputs.next(Statement.class),
                inputs.next(Variable.class)
            )
        );
        grammar.register(variable, pattern(WORD), input -> new Variable(input.next(String.class)));
        grammar.register(statement, pattern(curly(statement)),
            input -> new Block(input.next(Input.class).next(Statement.class))
        );
        grammar.register(statement, pattern("while", expression, statement),
            input -> new While(input.next(Expression.class), input.next(Statement.class))
        );
        grammar.register(statement, pattern(curly(statement, repeat(";", statement))),
            input -> new Block(input.consumeAll(Statement.class, true))
        );
        grammar.register(statement, pattern(WORD, ":=", expression),
            input -> new Assignment(input.next(String.class), input.next(Expression.class))
        );
        grammar.register(expression, pattern("nil"), _ -> new Nil());
        grammar.register(expression, pattern("cons", expression, expression),
            input -> new Cons(input.next(Expression.class), input.next(Expression.class))
        );
        grammar.register(expression, pattern("hd", expression),
            input -> new Head(input.next(Expression.class))
        );
        grammar.register(expression, pattern("tl", expression),
            input -> new Tail(input.next(Expression.class))
        );
        grammar.register(expression, pattern(variable), input -> input.next(Expression.class));
        {
            Model parsed = grammar.parse(root, "program read X { Y := X } write Y");
            assert parsed instanceof Program program
                && program.name.equals("program")
                && program.input.name.equals("X")
                && program.output.name.equals("Y")
                && program.body instanceof Block block
                && block.statements.length == 1
                && block.statements[0] instanceof Assignment assignment
                && assignment.name.equals("Y")
                && assignment.expression instanceof Variable variable
                && variable.name.equals("X");
        }
        {
            Model parsed = grammar.parse(root, "program read X { Z := X; Y := Z } write Y");
            assert parsed instanceof Program program
                && program.name.equals("program")
                && program.input.name.equals("X")
                && program.output.name.equals("Y")
                && program.body instanceof Block block
                && block.statements.length == 2
                && block.statements[0] instanceof Assignment assignment1
                && assignment1.name.equals("Z")
                && assignment1.expression instanceof Variable variable1
                && variable1.name.equals("X")
                && block.statements[1] instanceof Assignment assignment2
                && assignment2.name.equals("Y")
                && assignment2.expression instanceof Variable variable2
                && variable2.name.equals("Z");
        }
    }

    interface Expression extends ModelTest.SimpleModel {

    }

    interface Statement extends ModelTest.SimpleModel {

    }

    record Head(Expression expression) implements Expression {

    }

    record Tail(Expression expression) implements Expression {

    }

    record Block(Statement... statements) implements Statement {

    }

    record Assignment(String name, Expression expression) implements Statement {

    }

    record While(Expression expression, Statement statement) implements Statement {

    }

    record Cons(Expression first, Expression second) implements Expression {

    }

    record Nil() implements Expression {

    }

    record Variable(String name) implements Expression {

    }

    record Program(String name, Variable input, Statement body, Variable output) implements ModelTest.SimpleModel {

    }

}