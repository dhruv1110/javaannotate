package dev.dhruv.javaannotate.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import dev.dhruv.javaannotate.annotations.*;
import dev.dhruv.javaannotate.models.FieldModel;
import dev.dhruv.javaannotate.models.ModelsMap;

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
        "dev.dhruv.javaannotate.annotations.Models")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ModelProcessor extends AbstractProcessor {
    private Elements elements;
    private boolean isAllFieldConstructorCreated = false;
    private Filer filer;
    private Messager messager;
    private boolean isEmptyConstructorCreated = false;
    private Types types;
    private TypeSpec.Builder replicatedClassBuilder;
    private Map<String, FieldModel> replicatedClassFieldsMap;
    private List<FieldSpec> builderFieldSpecs = new ArrayList<>();

    private void createAllFieldConstructor() {
        isAllFieldConstructorCreated = true;

        MethodSpec.Builder getterMethodSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (FieldModel fieldModel : replicatedClassFieldsMap.values()) {
            if (!fieldModel.isFinal()) {
                ParameterSpec parameterSpec = ParameterSpec.builder(fieldModel.getTypeName(), fieldModel.getFieldName())
                        .build();
                getterMethodSpec.addParameter(parameterSpec);
                getterMethodSpec.addStatement("this." + fieldModel.getFieldName() + "=" + fieldModel.getFieldName());
            }
        }


        replicatedClassBuilder.addMethod(getterMethodSpec.build());
    }

    private void createBuilder(Model replicatedClassModel) {

        // create static builder class
        // initialize all the fields with null
        // create set methods for all the fields
        // create build() method and inside that create object with all argument constructor


        TypeSpec.Builder typeSpec = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        MethodSpec constuctor = MethodSpec.methodBuilder("Builder")
                .returns(ClassName.bestGuess("Builder"))
                .addStatement("return new Builder()")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build();

        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .returns(ClassName.bestGuess(replicatedClassModel.value()))
                .addStatement("return new " + replicatedClassModel.value() + "(this)")
                .addModifiers(Modifier.PUBLIC)
                .build();

        replicatedClassBuilder.addMethod(constuctor);
        typeSpec.addMethod(buildMethod);


        CodeBlock.Builder codeBlock = CodeBlock.builder();

        for (FieldSpec builderFieldSpec : builderFieldSpecs) {
            typeSpec.addField(builderFieldSpec);
            codeBlock.add("this." + builderFieldSpec.name + "=" + builderFieldSpec.name + ";");

            MethodSpec builderAddFieldMethod = MethodSpec.methodBuilder(builderFieldSpec.name)
                    .returns(ClassName.bestGuess("Builder"))
                    .addParameter(builderFieldSpec.type, builderFieldSpec.name)
                    .addStatement("this." + builderFieldSpec.name + "=" + builderFieldSpec.name)
                    .addStatement("return this")
                    .build();
            typeSpec.addMethod(builderAddFieldMethod);
        }
        MethodSpec initBuilderMethod = MethodSpec.constructorBuilder()
                .addParameter(ClassName.bestGuess("Builder"), "Builder")
                .addCode(codeBlock.build())
                .build();

        replicatedClassBuilder.addMethod(initBuilderMethod);
        replicatedClassBuilder.addType(typeSpec.build());
    }

    private void createClassElements(Model replicatedClassModel) {
        if (replicatedClassModel.emptyConstructor()) {
            createEmptyConstructor();
        }

        if (replicatedClassModel.allArgConstructor()) {
            createAllFieldConstructor();
        }

        if (replicatedClassModel.singleton()) {
            createSingleton(replicatedClassModel);
        }

        if (replicatedClassModel.builder()) {
            createBuilder(replicatedClassModel);
        }
    }

    private void createClasses(List<ModelsMap> modelsMaps) {

        // iterate through all @Models annotation
        for (ModelsMap modelsMap : modelsMaps) {

            // iterate through all @Model annotation
            for (Model replicatedClassModel : modelsMap.getReplicatedClasses()) {
                replicatedClassFieldsMap = new HashMap<>();
                isEmptyConstructorCreated = false;
                isAllFieldConstructorCreated = false;
                replicatedClassBuilder = TypeSpec
                        .classBuilder(replicatedClassModel.value())
                        .addModifiers(Modifier.PUBLIC);

                List<VariableElement> originalFields
                        = ElementFilter.fieldsIn(modelsMap.getMainClass().getEnclosedElements());

                // get all the Fields from the original class
                for (VariableElement originalField : originalFields) {

                    boolean createGetter = replicatedClassModel.getters();
                    boolean createSetter = replicatedClassModel.setters();
                    String fieldName = originalField.getSimpleName().toString();


                    if (originalField.getAnnotation(RenameField.class) != null) {
                        RenameField renameField = originalField.getAnnotation(RenameField.class);
                        if (renameField != null
                                && !renameField.value().isEmpty()) {
                            fieldName = renameField.value();
                        }
                    } else if (originalField.getAnnotation(Fields.class) != null) {
                        Field[] replicatedFieldsConfig = originalField.getAnnotation(Fields.class).value();
                        for (Field replicatedField : replicatedFieldsConfig) {
                            if (replicatedField.value().equalsIgnoreCase(replicatedClassModel.value())) {
                                fieldName = replicatedField.renameTo();
                                createGetter = replicatedField.getter();
                                createSetter = replicatedField.setter();
                            }
                        }
                    }


                    createElements(originalField, fieldName, createSetter, createGetter);


                }

                createClassElements(replicatedClassModel);

                try {
                    JavaFile.builder(modelsMap.getPackageName(), replicatedClassBuilder.build())
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Error creating replicated class, " + e.getMessage());
                }
            }
        }
    }

    private void createElements(VariableElement originalField, String fieldName, boolean createSetter, boolean createGetter) {

        createField(originalField, fieldName);
        String capitalizeFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        if (createSetter) {
            createSetter(originalField, fieldName, capitalizeFieldName);
        }
        if (createGetter) {
            createGetter(originalField, fieldName, capitalizeFieldName);
        }

        FieldModel fieldModel = new FieldModel(TypeName.get(originalField.asType()), fieldName, originalField.getModifiers().contains(Modifier.FINAL));
        replicatedClassFieldsMap.put(fieldName, fieldModel);
    }

    private void createEmptyConstructor() {
        isEmptyConstructorCreated = true;
        MethodSpec getterMethodSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
        replicatedClassBuilder.addMethod(getterMethodSpec);
    }

    private void createField(VariableElement originalField, String fieldName) {
        FieldSpec.Builder fieldSpec = FieldSpec.builder(TypeName.get(originalField.asType()), fieldName)
                .addModifiers(originalField.getModifiers().toArray(new Modifier[originalField.getModifiers().size()]));

        Object value = null;
        if (originalField.getConstantValue() != null
                && originalField.getModifiers().contains(Modifier.FINAL)) {
            value = originalField.getConstantValue();
            if (value instanceof String) {
                fieldSpec.initializer("$S", value);
            } else if (value instanceof Long) {
                fieldSpec.initializer("$LL", value);
            } else {
                fieldSpec.initializer("$L", value);
            }
        }
        replicatedClassBuilder.addField(fieldSpec.build());
        builderFieldSpecs.add(fieldSpec.build());
    }

    private void createGetter(VariableElement field, String fieldName, String capitalizeFieldName) {

        CodeBlock getterCode = CodeBlock.builder()
                .addStatement("return " + fieldName)
                .build();
        MethodSpec getterMethodSpec = MethodSpec.methodBuilder("get" + capitalizeFieldName)
                .addCode(getterCode)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(field.asType()))
                .build();
        replicatedClassBuilder.addMethod(getterMethodSpec);

    }

    private void createSetter(VariableElement field, String fieldName, String capitalizeFieldName) {

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


            replicatedClassBuilder.addMethod(setterMethodSpec);
        }
    }

    private void createSingleton(Model replicatedClassModel) {

        // create private final sInstance
        // create public method getInstance
        // inside the method, check if sInstance is null
        // if it's null, then initialize new object
        // return sInstance;

        String className = replicatedClassModel.value();

        FieldSpec sInstanceField = FieldSpec
                .builder(ClassName.bestGuess(className), "sInstance", Modifier.PRIVATE, Modifier.STATIC)
                .build();

        replicatedClassBuilder.addField(sInstanceField);

        CodeBlock codeBlock = CodeBlock.builder()
                .beginControlFlow("if(sInstance == null)")
                .addStatement(String.format("sInstance = new %s()", className))
                .endControlFlow()
                .addStatement("return sInstance")
                .build();

        MethodSpec getInstanceMethod = MethodSpec.methodBuilder("getInstance")
                .returns(ClassName.bestGuess(className))
                .addCode(codeBlock)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build();

        replicatedClassBuilder.addMethod(getInstanceMethod);

        if (!isEmptyConstructorCreated) {
            createEmptyConstructor();
        }
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
            List<Model> classNames = new ArrayList<>(Arrays.asList(element.getAnnotation(Models.class).value()));


            TypeElement typeElement = (TypeElement) element;


            String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();

            ModelsMap modelsMap = new ModelsMap();
            modelsMap.setMainClass(typeElement);
            modelsMap.setReplicatedClasses(classNames);
            modelsMap.setPackageName(packageName);
            modelsMaps.add(modelsMap);


        }


        createClasses(modelsMaps);
        return false;
    }
}
