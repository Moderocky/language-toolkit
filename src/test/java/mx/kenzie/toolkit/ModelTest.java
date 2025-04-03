package mx.kenzie.toolkit;

import mx.kenzie.toolkit.model.Model;
import mx.kenzie.toolkit.model.imitation.ModelProgram;
import mx.kenzie.toolkit.model.imitation.ModelStatement;
import mx.kenzie.toolkit.model.imitation.ProgramContext;
import org.valross.constantine.RecordConstant;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;

public interface ModelTest {


    interface SimpleModel extends Model {

        @Override
        default void print(PrintStream stream) {
            stream.print(this);
        }

    }

    interface SimpleProgramModel<Context extends ProgramContext> extends SimpleModel, ModelProgram<Context>, RecordConstant {

        ModelStatement<Context>[] statements();

        @Override
        default Iterator<ModelStatement<Context>> iterator() {
            return Arrays.stream(statements()).iterator();
        }

    }

}
