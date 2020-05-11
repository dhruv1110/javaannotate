package dev.dhruv.javaannotate.core.clazz;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

public abstract class ClassCreator {
    ClassName classObjectType;
    TypeSpec clazz;
    TypeSpec.Builder clazzBuilder;

    ClassCreator(TypeSpec.Builder clazzBuilder) {
        this.clazzBuilder = clazzBuilder;
        clazz = clazzBuilder.build();
        classObjectType = ClassName.bestGuess(clazz.name);
    }

    public abstract TypeSpec.Builder create();
}
