package mx.kenzie.toolkit.pattern;

import mx.kenzie.toolkit.error.ParsingException;
import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.parser.Unit;
import org.junit.Test;

import static mx.kenzie.toolkit.pattern.Pattern.*;

public class LambdaStructureTest {

    final Unit root = () -> "root", term = () -> "term", expression = () -> "expression";


    @Test
    public void simpleGrammar() throws ParsingException {
        Grammar grammar = new Grammar();
        grammar.register(root, pattern(WORD, "=", expression),
            inputs -> new Assignment(inputs.next(String.class), inputs.next(Expression.class))
        );
        grammar.register(term, pattern("\\", WORD, ".", expression),
            inputs -> new Function(inputs.next(String.class), inputs.next(Expression.class))
        );
        grammar.register(term, pattern(round(expression)), inputs -> inputs.next(Input.class).next(Expression.class));
        grammar.register(term, pattern(WORD), inputs -> new Variable(inputs.next(String.class)));
        grammar.register(expression, pattern(term, term),
            inputs -> new Application(inputs.next(Expression.class), inputs.next(Expression.class))
        );

        grammar.register(expression, pattern(term), inputs -> inputs.next(Expression.class));
        {
            Model parsed = grammar.parse(root, "a = b");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("a")
                && assignment.expression instanceof Variable variable
                && variable.name.equals("b");
        }
        {
            Model parsed = grammar.parse(root, "identity = \\x.x");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("identity")
                && assignment.expression instanceof Function function
                && function.variable.equals("x")
                && function.expression instanceof Variable variable
                && variable.name.equals("x");
        }
        {
            Model parsed = grammar.parse(root, "test = a b");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("test")
                && assignment.expression instanceof Application application
                && application.first instanceof Variable first
                && application.second instanceof Variable second
                && first.name.equals("a")
                && second.name.equals("b");
        }
        {
            Model parsed = grammar.parse(root, "test = a (b c)");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("test")
                && assignment.expression instanceof Application application
                && application.first instanceof Variable first
                && first.name.equals("a")
                && application.second instanceof Application inner
                && inner.first instanceof Variable b
                && inner.second instanceof Variable c
                && b.name.equals("b")
                && c.name.equals("c");
        }

    }

    @Test
    public void leftRecursiveGrammar() throws ParsingException {
        Grammar grammar = new Grammar();
        grammar.register(root, pattern(WORD, "=", expression),
            inputs -> new Assignment(inputs.next(String.class), inputs.next(Expression.class))
        );
        grammar.register(term, pattern("\\", WORD, ".", expression),
            inputs -> new Function(inputs.next(String.class), inputs.next(Expression.class))
        );
        grammar.register(term, pattern(round(expression)), inputs -> inputs.next(Input.class).next(Expression.class));
        grammar.register(term, pattern(WORD), inputs -> new Variable(inputs.next(String.class)));
        grammar.register(expression,
            pattern(pattern(term, term).leftRecursive(
                inputs -> new Application(inputs.next(Expression.class), inputs.next(Expression.class))
            )),
            inputs -> inputs.next(Input.class).next(Expression.class)
        );
        grammar.register(expression, pattern(term), inputs -> inputs.next(Expression.class));
        {
            Model parsed = grammar.parse(root, "a = b");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("a")
                && assignment.expression instanceof Variable variable
                && variable.name.equals("b");
        }
        {
            Model parsed = grammar.parse(root, "identity = \\x.x");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("identity")
                && assignment.expression instanceof Function function
                && function.variable.equals("x")
                && function.expression instanceof Variable variable
                && variable.name.equals("x");
        }
        {
            Model parsed = grammar.parse(root, "test = a b");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("test")
                && assignment.expression instanceof Application application
                && application.first instanceof Variable first
                && application.second instanceof Variable second
                && first.name.equals("a")
                && second.name.equals("b");
        }
        {
            Model parsed = grammar.parse(root, "test = a b c");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("test")
                && assignment.expression instanceof Application application
                && application.first instanceof Application inner
                && inner.first instanceof Variable a
                && inner.second instanceof Variable b
                && a.name.equals("a")
                && b.name.equals("b")
                && application.second instanceof Variable c
                && c.name.equals("c");
        }
        {
            Model parsed = grammar.parse(root, "test = a b c d");
            assert parsed instanceof Assignment assignment
                && assignment.name.equals("test")
                && assignment.expression instanceof Application application
                && application.first instanceof Application inner
                && inner.first instanceof Application inner2
                && inner2.first instanceof Variable a
                && inner2.second instanceof Variable b
                && a.name.equals("a")
                && b.name.equals("b")
                && inner.second instanceof Variable c
                && c.name.equals("c")
                && application.second instanceof Variable d
                && d.name.equals("d");
        }

    }

    interface Expression extends GrammarTest.SimpleModel {

    }

    record Function(String variable, Expression expression) implements Expression {

    }

    record Assignment(String name, Expression expression) implements GrammarTest.SimpleModel {

    }

    record Application(Expression first, Expression second) implements Expression {

    }

    record Variable(String name) implements Expression {

    }

}