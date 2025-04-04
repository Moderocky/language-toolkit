package mx.kenzie.toolkit.imitation;

import mx.kenzie.toolkit.model.imitation.ModelProgram;
import mx.kenzie.toolkit.model.imitation.ProgramContext;
import org.valross.foundation.Loader;
import org.valross.foundation.assembler.ClassFile;
import org.valross.foundation.assembler.code.OpCode;
import org.valross.foundation.assembler.tool.Access;
import org.valross.foundation.assembler.tool.ClassFileBuilder;
import org.valross.foundation.assembler.tool.CodeBuilder;
import org.valross.foundation.assembler.tool.MethodBuilder;
import org.valross.foundation.detail.Type;
import org.valross.foundation.detail.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

public class ImitationCompiler {

    protected final ModelProgram<?> program;
    private transient ClassFile data;

    public ImitationCompiler(ModelProgram<?> program) {
        this.program = program;
    }

    public void compile(OutputStream stream) throws IOException {
        this.build();
        this.data.write(stream);
    }

    public Class<?> load() throws IOException {
        this.build();
        return Loader.DEFAULT.loadClass(data);
    }

    public <Context extends ProgramContext> ModelProgram<Context> create() throws IOException {
        try {
            Class<?> loaded = this.load();
            Object object = loaded.getConstructor().newInstance();
            //noinspection unchecked
            return (ModelProgram<Context>) object;
        } catch (InvocationTargetException | NoSuchMethodException
                 | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized void build() {
        if (data != null) return;
        Type type = Type.of("imitation", "Program");

        ClassFileBuilder builder = new ClassFileBuilder(Version.JAVA_22, type);
        builder.setInterfaces(ModelProgram.class).setSuperType(Object.class);
        builder.addModifiers(Access.PUBLIC);

        builder.constructor()
            .setModifiers(Access.PUBLIC)
            .code().write(OpCode.ALOAD_0, OpCode.INVOKESPECIAL.constructor(Object.class), OpCode.RETURN);

        MethodBuilder method = builder.method();
        method.named("run").type(ProgramContext.class, ProgramContext.class);
        method.setModifiers(Access.PUBLIC);
        CodeBuilder code = method.code();
        assert program.validate() : program;
        code.write(OpCode.LDC.value(program), OpCode.ALOAD_1);
        code.write(OpCode.INVOKEINTERFACE.method(ModelProgram.class, ProgramContext.class, "run", ProgramContext.class));
        code.write(OpCode.ARETURN);

        this.data = builder.build();
    }

}
