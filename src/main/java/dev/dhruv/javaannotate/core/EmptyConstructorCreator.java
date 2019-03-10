package dev.dhruv.javaannotate.core;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

public class EmptyConstructorCreator extends MethodCreator {
    @Override
    public MethodSpec create() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
    }
}
