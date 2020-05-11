package dev.dhruv.javaannotate.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import dev.dhruv.javaannotate.annotations.*;
import dev.dhruv.javaannotate.core.clazz.BuilderCreator;
import dev.dhruv.javaannotate.core.clazz.ClassCreator;
import dev.dhruv.javaannotate.core.clazz.SingletonCreator;
import dev.dhruv.javaannotate.core.field.FieldCreator;
import dev.dhruv.javaannotate.core.method.*;
import dev.dhruv.javaannotate.models.FieldModel;
import dev.dhruv.javaannotate.models.ModelsMap;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@SupportedAnnotationTypes(
        "dev.dhruv.javaannotate.annotations.Models")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ModelProcessor extends AbstractProcessor {
    private MethodCreator methodCreator;
    private ClassCreator classCreator;
    private FieldCreator fieldCreator;
    private Elements elements;
    private Filer filer;
    private Messager messager;
    private TypeSpec.Builder replicatedClassBuilder;
    private Map<String, FieldModel> replicatedClassFieldsMap;

    private void createClassElements(Model replicatedClassModel) {
        if (replicatedClassModel.emptyConstructor()) {
            methodCreator = new EmptyConstructorCreator();
            replicatedClassBuilder.addMethod(methodCreator.create());
        }

        if (replicatedClassModel.allArgConstructor()) {
            methodCreator = new AllFieldConstructorCreator(replicatedClassFieldsMap.values());
            replicatedClassBuilder.addMethod(methodCreator.create());
        }

        if (replicatedClassModel.singleton()) {
            classCreator = new SingletonCreator(replicatedClassBuilder);
            replicatedClassBuilder = classCreator.create();
        }

        if (replicatedClassModel.builder()) {
            classCreator = new BuilderCreator(replicatedClassBuilder);
            replicatedClassBuilder = classCreator.create();
        }
    }

    private void createClasses(List<ModelsMap> modelsMaps) {

        // iterate through all @Models annotation
        for (ModelsMap modelsMap : modelsMaps) {

            // iterate through all @Model annotation
            for (Model replicatedClassModel : modelsMap.getReplicatedClasses()) {
                replicatedClassFieldsMap = new HashMap<>();
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

        boolean isFinal = originalField.getModifiers().contains(Modifier.FINAL);

        fieldCreator = new FieldCreator(originalField, fieldName);
        replicatedClassBuilder.addField(fieldCreator.create());
        if (createSetter && !isFinal) {
            methodCreator = new SetterCreator(originalField, fieldName);
            replicatedClassBuilder.addMethod(methodCreator.create());
        }
        if (createGetter) {
            methodCreator = new GetterCreator(originalField, fieldName);
            replicatedClassBuilder.addMethod(methodCreator.create());
        }

        FieldModel fieldModel = new FieldModel(TypeName.get(originalField.asType()), fieldName, originalField.getModifiers().contains(Modifier.FINAL));
        replicatedClassFieldsMap.put(fieldName, fieldModel);
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();

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
