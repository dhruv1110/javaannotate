package dev.dhruv.javaannotate.core.clazz;

import com.squareup.javapoet.*;
import dev.dhruv.javaannotate.core.method.EmptyConstructorCreator;

import javax.lang.model.element.Modifier;

public class SingletonCreator extends ClassCreator {
    public final static String S_INSTANCE = "sInstance";

    public SingletonCreator(TypeSpec.Builder clazzBuilder) {
        super(clazzBuilder);
    }

    public TypeSpec.Builder create() {


        createInstanceField();
        createGetInstanceMethod();

        if (!isEmptyConstructorCreated()) {
            MethodSpec constructor = new EmptyConstructorCreator().create();
            clazzBuilder.addMethod(constructor);
        }
        return clazzBuilder;
    }

    private void createGetInstanceMethod() {
        CodeBlock codeBlock = CodeBlock.builder()
                .beginControlFlow("if(" + S_INSTANCE + " == null)")
                .addStatement(String.format(S_INSTANCE + " = new %s()", clazz.name))
                .endControlFlow()
                .addStatement("return " + S_INSTANCE)
                .build();

        MethodSpec getInstanceMethod = MethodSpec.methodBuilder("getInstance")
                .returns(ClassName.bestGuess(clazz.name))
                .addCode(codeBlock)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build();

        clazzBuilder.addMethod(getInstanceMethod);
    }

    private void createInstanceField() {
        FieldSpec sInstanceField = FieldSpec
                .builder(classObjectType, S_INSTANCE, Modifier.PRIVATE, Modifier.STATIC)
                .build();

        clazzBuilder.addField(sInstanceField);
    }

    private boolean isEmptyConstructorCreated() {

        TypeSpec typeSpec = clazzBuilder.build();

        for (MethodSpec methodSpec : typeSpec.methodSpecs) {
            if ("<init>".equalsIgnoreCase(methodSpec.name)
                    && methodSpec.parameters.isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
