package mx.kenzie.toolkit.model.imitation;

import mx.kenzie.toolkit.model.Model;

public interface ModelProgram<Context extends ProgramContext> extends Model, Iterable<ModelStatement<Context>>, ModelStatement<Context> {

    @Override
    default Context run(Context context) throws Throwable {
        for (ModelStatement<Context> statement : this) {
            context = statement.run(context);
        }
        return context;
    }

}
