package mx.kenzie.toolkit.imitation;

import mx.kenzie.toolkit.model.imitation.ModelProgram;
import mx.kenzie.toolkit.model.imitation.ProgramContext;
import mx.kenzie.toolkit.parser.Unit;
import mx.kenzie.toolkit.pattern.Grammar;
import mx.kenzie.toolkit.pattern.Input;
import org.junit.Test;

import static mx.kenzie.toolkit.pattern.Pattern.*;

public class ImitationLambdaStructureTest extends ImitationCompilerTest {

    final Unit term = () -> "term";

    @Override
    protected Grammar load() {
        Grammar grammar = new Grammar();
        grammar.register(root, pattern(repeat(statement)),
            input -> new Program(input.consumeAll(Statement.class, true))
        );
        grammar.register(statement, pattern(WORD, "=", expression),
            inputs -> new Assignment(inputs.next(String.class), inputs.next(Expression.class))
        );
        grammar.register(statement, pattern(expression),
            inputs -> new Evaluation(inputs.next(Expression.class))
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
        return grammar;
    }

    @Test
    public void simpleModel() throws Throwable {
        Grammar grammar = this.load();
        ModelProgram<?> parsed = (ModelProgram<?>) grammar.parse(root, """
            id = \\x.x
            """);
        ModelProgram<ProgramContext> program = new ImitationCompiler(parsed).create();
        TestContext context = new TestContext();
        program.run(context);
        assert context.variables.get("id") instanceof Function function
            && function.variable.equals("x")
            && function.expression instanceof Variable variable
            && variable.name.equals("x");
    }

    @Test
    public void withApplication() throws Throwable {
        Grammar grammar = this.load();
        ModelProgram<?> parsed = (ModelProgram<?>) grammar.parse(root, """
            (\\x.x) (\\x.x)
            """);
        ModelProgram<ProgramContext> program = new ImitationCompiler(parsed).create();
        TestContext context = new TestContext();
        program.run(context);
        assert context.output instanceof Function function
            && function.variable.equals("x")
            && function.expression instanceof Variable variable
            && variable.name.equals("x");
    }

    @Test
    public void withApplicationDuplicate() throws Throwable {
        Grammar grammar = this.load();
        ModelProgram<?> parsed = (ModelProgram<?>) grammar.parse(root, """
            (\\x.x x) (\\x.x)
            """);
        ModelProgram<ProgramContext> program = new ImitationCompiler(parsed).create();
        TestContext context = new TestContext();
        program.run(context);
        assert context.output instanceof Function function
            && function.variable.equals("x")
            && function.expression instanceof Variable variable
            && variable.name.equals("x");
    }

    @Test
    public void complex() throws Throwable {
        Grammar grammar = this.load();
        ModelProgram<?> parsed = (ModelProgram<?>) grammar.parse(root, """
            (\\p.\\q.p q p) (\\x.\\y.x) (\\x.\\y.y)
            """);
        ModelProgram<ProgramContext> program = new ImitationCompiler(parsed).create();
        TestContext context = new TestContext();
        program.run(context);
        assert context.output instanceof Function function
            && function.variable.equals("x")
            && function.expression instanceof Function second
            && second.variable.equals("y")
            && second.expression instanceof Variable variable
            && variable.name.equals("y")
            : context.output;
    }

    public record Function(String variable, Expression expression) implements Expression {

        @Override
        public Object evaluate(TestContext context) throws Throwable {
            return new Function(variable, (Expression) expression.evaluate(context));
        }

    }

    public record Assignment(String name, Expression expression) implements Statement {

        @Override
        public TestContext run(TestContext context) throws Throwable {
            context.variables.put(name, expression.evaluate(context));
            return context;
        }

    }

    public record Evaluation(Expression expression) implements Statement {

        @Override
        public TestContext run(TestContext context) throws Throwable {
            context.output = expression.evaluate(context);
            return context;
        }

    }

    public record Application(Expression first, Expression second) implements Expression {

        @Override
        public Object evaluate(TestContext context) throws Throwable {
            Expression a = (Expression) first.evaluate(context);
            Expression b = (Expression) second.evaluate(context);
            if (a instanceof Function function) {
                TestContext inner = new TestContext();
                inner.variables.putAll(context.variables);
                inner.variables.put(function.variable, b);
                return function.expression.evaluate(inner);
//                Expression evaluated = (Expression) function.expression.evaluate(inner);
//                return evaluated.evaluate(context);
            }
            return new Application(a, b);
        }

    }

    public record Variable(String name) implements Expression {

        @Override
        public Object evaluate(TestContext context) throws Throwable {
            return context.variables.getOrDefault(name, this);
        }

    }

}