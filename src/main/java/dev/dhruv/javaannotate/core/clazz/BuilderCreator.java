package dev.dhruv.javaannotate.core.clazz;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class BuilderCreator extends ClassCreator {
    public final static String BUILDER = "Builder";

    public BuilderCreator(TypeSpec.Builder clazzBuilder) {
        super(clazzBuilder);
    }

    @Override
    public TypeSpec.Builder create() {
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(BUILDER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        MethodSpec constructor = MethodSpec.methodBuilder(BUILDER)
                .returns(ClassName.bestGuess(BUILDER))
                .addStatement("return new " + BUILDER + "()")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build();

        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .returns(classObjectType)
                .addStatement("return new " + clazz.name + "(this)")
                .addModifiers(Modifier.PUBLIC)
                .build();

        clazzBuilder.addMethod(constructor);
        typeSpec.addMethod(buildMethod);


        CodeBlock.Builder codeBlock = CodeBlock.builder();
        for (FieldSpec builderFieldSpec : clazz.fieldSpecs) {
            if (builderFieldSpec.modifiers.contains(Modifier.FINAL)
                    || builderFieldSpec.name.equalsIgnoreCase(SingletonCreator.S_INSTANCE)) {
                continue;
            }
            typeSpec.addField(builderFieldSpec);
            codeBlock.add("this." + builderFieldSpec.name + "=" + builderFieldSpec.name + ";\n");

            MethodSpec builderAddFieldMethod = MethodSpec.methodBuilder(builderFieldSpec.name)
                    .returns(ClassName.bestGuess(BUILDER))
                    .addParameter(builderFieldSpec.type, builderFieldSpec.name)
                    .addStatement("this." + builderFieldSpec.name + "=" + builderFieldSpec.name)
                    .addStatement("return this")
                    .build();
            typeSpec.addMethod(builderAddFieldMethod);
        }
        MethodSpec initBuilderMethod = MethodSpec.constructorBuilder()
                .addParameter(ClassName.bestGuess(BUILDER), "Builder")
                .addCode(codeBlock.build())
                .build();

        clazzBuilder.addMethod(initBuilderMethod);
        clazzBuilder.addType(typeSpec.build());
        return clazzBuilder;
    }
}
