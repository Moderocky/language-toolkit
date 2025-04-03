package mx.kenzie.toolkit.model.imitation;

import mx.kenzie.toolkit.model.Model;
import org.valross.constantine.Constant;

public interface ModelExpression<Context extends ProgramContext, Result> extends Model, Constant {

    Result evaluate(Context context) throws Throwable;

}
