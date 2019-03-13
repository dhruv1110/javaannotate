package dev.dhruv.javaannotate.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;
import dev.dhruv.javaannotate.annotations.Getter;
import dev.dhruv.javaannotate.core.clazz.ClassCreator;
import dev.dhruv.javaannotate.core.field.FieldCreator;
import dev.dhruv.javaannotate.core.method.GetterCreator;
import dev.dhruv.javaannotate.core.method.MethodCreator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes(
        "dev.dhruv.javaannotate.annotations.Getter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GetterProcessor extends AbstractProcessor {
    private ClassCreator classCreator;
    private Elements elements;
    private FieldCreator fieldCreator;
    private Filer filer;
    private Messager messager;
    private MethodCreator methodCreator;

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
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Getter.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            TypeSpec.Builder clazzBuilder = TypeSpec
                    .classBuilder(typeElement.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC);

            Element[] elements1 = typeElement.getEnclosedElements().toArray(new Element[typeElement.getEnclosedElements().size()]);

            String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();

            List<VariableElement> originalFields
                    = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

            for (VariableElement originalField : originalFields) {
                methodCreator = new GetterCreator(originalField, originalField.getSimpleName().toString());

                clazzBuilder.addMethod(methodCreator.create());

                JavaFileObject javaFileObject
                        = null;
                try {
                    FileObject fileObject
                            = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, typeElement.getSimpleName().toString());
                    boolean isFileDeleted = fileObject.delete();
                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Deleted ==> " + isFileDeleted);
                    javaFileObject = filer.createSourceFile(packageName + "." + typeElement.getSimpleName().toString(), elements1);
                    Writer writer = javaFileObject.openWriter();
                    writer.write(clazzBuilder.build().toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        }
        return false;
    }
}
