package dev.dhruv.javaannotate.core.methods;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;

public abstract class MethodCreator {

    VariableElement classFieldElement;
    String fieldName;
    private String capitalizeFieldName;

    MethodCreator(VariableElement classFieldElement, String fieldName) {
        this.classFieldElement = classFieldElement;
        this.fieldName = fieldName;
    }

    MethodCreator() {
    }

    public abstract MethodSpec create();

    String getCapitalizeFieldName() {
        if (capitalizeFieldName == null) {
            capitalizeFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        return capitalizeFieldName;
    }

    void validate() {
        if (classFieldElement == null || fieldName == null) {
            throw new NullPointerException("Null values in MethodCreator");
        }
        if (fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be empty");
        }
    }
}
