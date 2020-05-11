package dev.dhruv.javaannotate.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import dev.dhruv.javaannotate.annotations.Getter;
import dev.dhruv.javaannotate.utils.ElementUtils;
import dev.dhruv.javaannotate.utils.JavacUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes(
        "dev.dhruv.javaannotate.annotations.Getter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GetterProcessor extends BaseJavacProcessor {


    private JCTree.JCMethodDecl createGetterMethod(VariableElement variableElement) {

        JCTree.JCVariableDecl variableDecl = JavacUtils.getVariableDeclaration(trees, variableElement);

        Name name = javacElements.getName(ElementUtils.getterMathodName(variableElement));
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(java.lang.reflect.Modifier.PUBLIC);

        ListBuffer<JCTree.JCTypeParameter> parameters = new ListBuffer<>();
        ListBuffer<JCTree.JCVariableDecl> paramValues = new ListBuffer<>();
        ListBuffer<JCTree.JCExpression> var6 = new ListBuffer<>();
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        JCTree.JCReturn jcReturn = treeMaker.Return(treeMaker.Ident(javacElements.getName(variableElement.getSimpleName().toString())));
        statements.add(jcReturn);
        JCTree.JCBlock jcBlock = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(modifiers
                , name
                , variableDecl.vartype
                , parameters.toList()
                , paramValues.toList()
                , var6.toList()
                , jcBlock
                , null);

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


            List<VariableElement> originalFields = ElementUtils.getFields(element);
            JCTree.JCClassDecl classDecl = JavacUtils.getClassDeclaration(trees, element);
            for (VariableElement originalField : originalFields) {

                JCTree.JCMethodDecl jcVariableDecl = createGetterMethod(originalField);
                classDecl.defs = classDecl.defs.prepend(jcVariableDecl);

            }
        }
        return false;
    }
}
