package dev.dhruv.javaannotate.models;

import com.squareup.javapoet.TypeName;

public class FieldModel {
    private TypeName typeName;

    private String fieldName;

    private boolean isFinal = false;

    public FieldModel(TypeName typeName, String fieldName, boolean isFinal) {
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.isFinal = isFinal;
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isFinal() {
        return isFinal;
    }
}


