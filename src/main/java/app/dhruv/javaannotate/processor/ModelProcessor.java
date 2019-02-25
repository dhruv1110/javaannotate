package app.dhruv.javaannotate.processor;

import app.dhruv.javaannotate.annotations.Model;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@SupportedAnnotationTypes(
        "app.dhruv.javaannotate.annotations.Model")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ModelProcessor extends AbstractProcessor {
    private Messager messager;
    private Elements elements;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();

    }
//
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        return Collections.singleton(Model.class.getCanonicalName());
//    }
//
//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.latestSupported();
//    }
    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.WARNING, "Searching==================");

        for (Element element : roundEnv.getElementsAnnotatedWith(Model.class)) {
            if (element.getKind() != ElementKind.CLASS){
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }
            String[] classNames = element.getAnnotation(Model.class).value();


            TypeElement typeElement = (TypeElement) element;



            String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();

            for (String className : classNames) {
                System.out.println("Class name = " + className);
                messager.printMessage(Diagnostic.Kind.WARNING, "Class name = " + className);
                TypeSpec.Builder modelClass = TypeSpec
                        .classBuilder(className)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                try {
                    JavaFile.builder(packageName, modelClass.build())
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }
}
