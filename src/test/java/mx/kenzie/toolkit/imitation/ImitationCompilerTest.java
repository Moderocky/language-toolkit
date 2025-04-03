package mx.kenzie.toolkit.imitation;

import mx.kenzie.toolkit.ModelTest;
import mx.kenzie.toolkit.model.imitation.ModelExpression;
import mx.kenzie.toolkit.model.imitation.ModelProgram;
import mx.kenzie.toolkit.model.imitation.ModelStatement;
import mx.kenzie.toolkit.model.imitation.ProgramContext;
import mx.kenzie.toolkit.parser.Unit;
import mx.kenzie.toolkit.pattern.Grammar;
import org.junit.Test;
import org.valross.constantine.RecordConstant;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import static mx.kenzie.toolkit.pattern.Pattern.*;

public class ImitationCompilerTest implements ModelTest {

    final Unit root = () -> "program", statement = () -> "statement",
        expression = () -> "expression";

    protected Grammar load() {
        Grammar grammar = new Grammar();
        grammar.register(expression, pattern(WORD), input -> new Variable(input.next(String.class)));
        grammar.register(statement, pattern("return", expression),
            input -> new Return(input.next(Expression.class))
        );
        grammar.register(statement, pattern(WORD, "=", expression),
            input -> new Assignment(input.next(String.class), input.next(Expression.class))
        );
        grammar.register(root, pattern("program", repeat(statement)),
            input -> new Program(input.consumeAll(Statement.class, true))
        );
        return grammar;
    }

    @Test
    public void create() throws Throwable {
        try {
            MethodHandles.lookup().findConstructor(Assignment.class, MethodType.methodType(void.class, new Assignment("", null).canonicalParameters()));
        } catch (Throwable ex) {
            ex.printStackTrace();
            assert false;
        }
        Grammar grammar = this.load();
        ModelProgram<?> parsed = (ModelProgram<?>) grammar.parse(root, """
            program
            y = x
            return y
            """);
        ModelProgram<ProgramContext> program = new ImitationCompiler(parsed).create();
        TestContext context = new TestContext();
        context.variables.put("x", 5);
        assert context.variables.get("x").equals(5);
        assert context.variables.get("y") == null;
        program.run(context);
        assert context.variables.get("x").equals(5);
        assert context.variables.get("y").equals(5);
        assert context.output.equals(5);
    }


    protected static class TestContext implements ProgramContext {

        public final Map<String, Object> variables = new HashMap<>();
        public volatile Object output;

    }

    protected interface Expression extends ModelExpression<TestContext, Object>, SimpleModel, RecordConstant {

    }

    protected interface Statement extends ModelStatement<TestContext>, RecordConstant, SimpleModel {

    }

    public record Variable(String name) implements Expression {


        @Override
        public Object evaluate(TestContext context) throws Throwable {
            return context.variables.get(name);
        }

    }

    public record Assignment(String name,
                             Expression value) implements Statement {

        @Override
        public TestContext run(TestContext context) throws Throwable {
            context.variables.put(name, value.evaluate(context));
            return context;
        }

    }

    public record Return(Expression value) implements Statement {

        @Override
        public TestContext run(TestContext context) throws Throwable {
            context.output = value.evaluate(context);
            return context;
        }

    }


    public record Program(Statement... statements) implements SimpleProgramModel<TestContext>, Statement {

        @SafeVarargs
        public Program {
        }

    }

}