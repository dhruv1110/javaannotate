package dev.dhruv.javaannotate.core;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dev.dhruv.javaannotate.models.FieldModel;

import javax.lang.model.element.Modifier;
import java.util.Collection;

public class AllFieldConstructorCreator extends MethodCreator {

    private Collection<FieldModel> fieldModels;

    public AllFieldConstructorCreator(Collection<FieldModel> fieldModels) {
        this.fieldModels = fieldModels;
    }

    @Override
    public MethodSpec create() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (FieldModel fieldModel : fieldModels) {
            if (!fieldModel.isFinal()) {
                ParameterSpec parameterSpec = ParameterSpec.builder(fieldModel.getTypeName(), fieldModel.getFieldName())
                        .build();
                constructor.addParameter(parameterSpec);
                constructor.addStatement("this." + fieldModel.getFieldName() + "=" + fieldModel.getFieldName() + "\n");
            }
        }
        return constructor.build();
    }
}
