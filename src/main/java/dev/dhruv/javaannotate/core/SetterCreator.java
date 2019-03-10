package dev.dhruv.javaannotate.core;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class SetterCreator extends MethodCreator {

    public SetterCreator(VariableElement classFieldElement, String fieldName) {
        super(classFieldElement, fieldName);
    }

    @Override
    public MethodSpec create() {
        validate();
        if (classFieldElement.getModifiers().contains(Modifier.FINAL)) {
            return null;
        }
        CodeBlock setterCode = CodeBlock.builder()
                .addStatement("this." + fieldName + "=" + fieldName)
                .build();
        return MethodSpec.methodBuilder("set" + getCapitalizeFieldName())
                .addCode(setterCode)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(classFieldElement.asType()), fieldName)
                .returns(void.class)
                .build();

    }
}
