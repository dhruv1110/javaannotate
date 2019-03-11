package dev.dhruv.javaannotate.core.field;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class FieldCreator {

    VariableElement classFieldElement;
    String fieldName;

    public FieldCreator(VariableElement classFieldElement, String fieldName) {
        this.classFieldElement = classFieldElement;
        this.fieldName = fieldName;
    }


    public FieldSpec create() {
        FieldSpec.Builder fieldSpec = FieldSpec.builder(TypeName.get(classFieldElement.asType()), fieldName)
                .addModifiers(classFieldElement.getModifiers().toArray(new Modifier[classFieldElement.getModifiers().size()]));

        Object value = null;
        if (classFieldElement.getConstantValue() != null
                && classFieldElement.getModifiers().contains(Modifier.FINAL)) {
            value = classFieldElement.getConstantValue();
            if (value instanceof String) {
                fieldSpec.initializer("$S", value);
            } else if (value instanceof Long) {
                fieldSpec.initializer("$LL", value);
            } else {
                fieldSpec.initializer("$L", value);
            }
        }
        return fieldSpec.build();
    }
}
