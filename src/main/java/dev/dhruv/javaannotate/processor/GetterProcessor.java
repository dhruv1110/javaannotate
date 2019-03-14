package dev.dhruv.javaannotate.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
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

    private JavacElements javacElements;
    private TreeMaker treeMaker;
    private Trees trees;

    private JCTree.JCVariableDecl createHelloWorld(VariableElement variableElement) {
        Name name = javacElements.getName("test");
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(java.lang.reflect.Modifier.PUBLIC);
        JCTree.JCIdent jcIdent = treeMaker.Ident(javacElements.getName("String"));
        JCTree.JCVariableDecl jcVariableDecl = treeMaker.VarDef(modifiers, name, jcIdent, null);
        return jcVariableDecl;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        this.trees = Trees.instance(processingEnv);
        // 这个强制转换是个trick, 使得processor能对java的parse tree做更改
        Context ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(ctx);
        this.javacElements = JavacElements.instance(ctx);

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

//            Element[] elements1 = typeElement.getEnclosedElements().toArray(new Element[typeElement.getEnclosedElements().size()]);

            String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();

            List<VariableElement> originalFields
                    = ElementFilter.fieldsIn(typeElement.getEnclosedElements());


            Tree tree = trees.getTree(typeElement);
            JCTree jcTree = (JCTree) tree;
            JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) tree;
            for (JCTree def : classDecl.defs) {

                messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Defination ==> " + def.toString());
            }
            for (VariableElement originalField : originalFields) {
                methodCreator = new GetterCreator(originalField, originalField.getSimpleName().toString());

                clazzBuilder.addMethod(methodCreator.create());

                fieldCreator = new FieldCreator(originalField, originalField.getSimpleName().toString());
                clazzBuilder.addField(fieldCreator.create());

                JCTree.JCVariableDecl jcVariableDecl = createHelloWorld(originalField);
                classDecl.defs.append(jcVariableDecl);


            }
//            JavaFileObject javaFileObject
//                    = null;
//            try {
//                FileObject fileObject
//                        = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, typeElement.getSimpleName().toString());
//                boolean isFileDeleted = fileObject.delete();
//                messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Deleted ==> " + isFileDeleted);
//                javaFileObject = filer.createSourceFile(packageName + "." + typeElement.getSimpleName().toString());
//                Writer writer = javaFileObject.openWriter();
//                writer.write(clazzBuilder.build().toString());
//                writer.flush();
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "JCtree ==> " + classDecl.name.toString());
        }
        return false;
    }
}
