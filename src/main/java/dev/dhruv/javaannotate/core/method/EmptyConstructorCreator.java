package dev.dhruv.javaannotate.core.method;

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
