package dev.dhruv.javaannotate.core;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class GetterCreator extends MethodCreator {
    public GetterCreator(VariableElement classFieldElement, String fieldName) {
        super(classFieldElement, fieldName);
    }

    @Override
    public MethodSpec create() {
        validate();
        CodeBlock getterCode = CodeBlock.builder()
                .addStatement("return " + fieldName)
                .build();
        return MethodSpec.methodBuilder("get" + getCapitalizeFieldName())
                .addCode(getterCode)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(classFieldElement.asType()))
                .build();
    }
}
