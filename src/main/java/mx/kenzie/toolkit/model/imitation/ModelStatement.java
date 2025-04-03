package mx.kenzie.toolkit.model.imitation;

import mx.kenzie.toolkit.model.Model;
import org.valross.constantine.Constant;

public interface ModelStatement<Context extends ProgramContext> extends Model, Constant {

    Context run(Context context) throws Throwable;

}
