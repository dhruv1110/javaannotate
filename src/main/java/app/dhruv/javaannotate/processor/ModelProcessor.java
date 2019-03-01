package app.dhruv.javaannotate.processor;

import app.dhruv.javaannotate.annotations.FieldOf;
import app.dhruv.javaannotate.annotations.Model;
import app.dhruv.javaannotate.annotations.Models;
import app.dhruv.javaannotate.models.FieldModel;
import app.dhruv.javaannotate.models.ModelsMap;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@SupportedAnnotationTypes(
        "app.dhruv.javaannotate.annotations.Models")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ModelProcessor extends AbstractProcessor {
    private Elements elements;
    private Filer filer;
    private Messager messager;
    private Types types;
    private TypeSpec.Builder modelClass;
    private Map<String, FieldModel> fieldModelsMap;

    private void createClasses(List<ModelsMap> modelsMaps) {
        for (ModelsMap modelsMap : modelsMaps) {
            fieldModelsMap = new HashMap<>();
            for (String className : modelsMap.getReplicatedClasses()) {
                modelClass = TypeSpec
                        .classBuilder(className)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

                List<VariableElement> allFields = ElementFilter.fieldsIn(modelsMap.getMainClass().getEnclosedElements());
                for (VariableElement field : allFields) {
                    String fieldName = field.getSimpleName().toString();
                    Object value = null;


                    FieldSpec.Builder fieldSpec = FieldSpec.builder(TypeName.get(field.asType()), fieldName)
                            .addModifiers(field.getModifiers().toArray(new Modifier[field.getModifiers().size()]));

                    if (field.getConstantValue() != null
                            && field.getModifiers().contains(Modifier.FINAL)){
                        value = field.getConstantValue();
                        if (value instanceof String){
                            fieldSpec.initializer("$S", value);
                        } else if (value instanceof Long){
                            fieldSpec.initializer("$LL", value);
                        } else {
                            fieldSpec.initializer("$L", value);
                        }
                    }
                    modelClass.addField(fieldSpec.build());

                    String capitalizeFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    createSetter(field, fieldName, capitalizeFieldName);
                    createGetter(field, fieldName, capitalizeFieldName);

                    FieldModel fieldModel = new FieldModel(TypeName.get(field.asType()), fieldName, field.getModifiers().contains(Modifier.FINAL));
                    fieldModelsMap.put(fieldName, fieldModel);


                }
                createEmptyConstructor();
                createAllFieldConstructor();

                try {
                    JavaFile.builder(modelsMap.getPackageName(), modelClass.build())
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createAllFieldConstructor() {

        MethodSpec.Builder getterMethodSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (FieldModel fieldModel : fieldModelsMap.values()) {
            if (!fieldModel.isFinal()) {
                ParameterSpec parameterSpec = ParameterSpec.builder(fieldModel.getTypeName(), fieldModel.getFieldName())
                        .build();
                getterMethodSpec.addParameter(parameterSpec);
                getterMethodSpec.addStatement("this." + fieldModel.getFieldName() + "=" + fieldModel.getFieldName());
            }
        }


        modelClass.addMethod(getterMethodSpec.build());
    }

    private void createEmptyConstructor() {
        MethodSpec getterMethodSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
        modelClass.addMethod(getterMethodSpec);
    }

    private void createSetter(VariableElement field, String fieldName, String capitalizeFieldName){
        if (field.getAnnotation(FieldOf.class) != null){
            FieldOf fieldOf = field.getAnnotation(FieldOf.class);
            if (!fieldOf.setter()){
                return;
            }
        }
        if (!field.getModifiers().contains(Modifier.FINAL)) {
            CodeBlock setterCode = CodeBlock.builder()
                    .addStatement("this." + fieldName + "=" + fieldName)
                    .build();
            MethodSpec setterMethodSpec = MethodSpec.methodBuilder("set" + capitalizeFieldName)
                    .addCode(setterCode)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(field.asType()), fieldName)
                    .returns(void.class)
                    .build();


            modelClass.addMethod(setterMethodSpec);
        }
    }

    private void createGetter(VariableElement field, String fieldName, String capitalizeFieldName){
        if (field.getAnnotation(FieldOf.class) != null){
            FieldOf fieldOf = field.getAnnotation(FieldOf.class);
            if (!fieldOf.getter()){
                return;
            }
        }
        CodeBlock getterCode = CodeBlock.builder()
                .addStatement("return " +  fieldName)
                .build();
        MethodSpec getterMethodSpec = MethodSpec.methodBuilder("get" + capitalizeFieldName)
                .addCode(getterCode)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(field.asType()))
                .build();
        modelClass.addMethod(getterMethodSpec);

    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();

    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<ModelsMap> modelsMaps = new ArrayList<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(Models.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }
//            String[] classNames = element.getAnnotation(Models.class).value()[0].value();
            List<String> classNames = new ArrayList<>();
            for (Model model : element.getAnnotation(Models.class).value()) {
                classNames.add(model.value());
            }


            TypeElement typeElement = (TypeElement) element;


            String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();

            ModelsMap modelsMap = new ModelsMap();
            modelsMap.setMainClass(typeElement);
            modelsMap.setReplicatedClasses(classNames);
            modelsMap.setPackageName(packageName);
            modelsMaps.add(modelsMap);

//            for (String className : classNames) {
//                System.out.println("Class name = " + className);
//                messager.printMessage(Diagnostic.Kind.WARNING, "Class name = " + className);
//                TypeSpec.Builder modelClass = TypeSpec
//                        .classBuilder(className)
//                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//
//                for (Element enclosedElement : typeElement.getEnclosedElements()) {
//                }
//                try {
//                    JavaFile.builder(packageName, modelClass.build())
//                            .build()
//                            .writeTo(filer);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

        }


        createClasses(modelsMaps);
        return false;
    }
}
