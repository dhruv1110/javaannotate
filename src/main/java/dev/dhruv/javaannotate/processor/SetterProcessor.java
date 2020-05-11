package dev.dhruv.javaannotate.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import dev.dhruv.javaannotate.annotations.Setter;
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
import java.util.Set;

@SupportedAnnotationTypes(
        "dev.dhruv.javaannotate.annotations.Setter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class SetterProcessor extends BaseJavacProcessor {


    private JCTree.JCMethodDecl createSetterMethod(VariableElement variableElement) {


        JCTree.JCVariableDecl variableDecl = JavacUtils.getVariableDeclaration(trees, variableElement);

        Name methodName = javacElements.getName(ElementUtils.setterMathodName(variableElement));
        JCTree.JCModifiers methodModifiers = treeMaker.Modifiers(java.lang.reflect.Modifier.PUBLIC);

        ListBuffer<JCTree.JCVariableDecl> methodParameters = new ListBuffer<>();
        methodParameters.add(
                treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER)
                        , javacElements.getName(variableElement.getSimpleName().toString())
                        , treeMaker.Type(variableDecl.vartype.type)
                        , null));


        return treeMaker.MethodDef(methodModifiers
                , methodName
                , treeMaker.TypeIdent(TypeTag.VOID)
                , List.nil()
                , methodParameters.toList()
                , List.nil()
                , createSetterMethodBody(variableDecl)
                , null);

    }


    private JCTree.JCBlock createSetterMethodBody(JCTree.JCVariableDecl variableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();


        JCTree.JCExpression chain = treeMaker.This(variableDecl.vartype.type);

        chain = treeMaker.Select(chain, variableDecl.name);
        chain = treeMaker.Assign(chain, treeMaker.Ident(variableDecl));
        statements.add(treeMaker.Exec(chain));
        return treeMaker.Block(0, statements.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Setter.class)) {

            java.util.List<VariableElement> originalFields = ElementUtils.getFields(element);
            JCTree.JCClassDecl classDecl = JavacUtils.getClassDeclaration(trees, element);
            for (VariableElement originalField : originalFields) {

                if (originalField.getModifiers().contains(javax.lang.model.element.Modifier.FINAL)) {
                    continue;
                }

                JCTree.JCMethodDecl jcVariableDecl = createSetterMethod(originalField);
                classDecl.defs = classDecl.defs.prepend(jcVariableDecl);

            }
        }
        return false;
    }
}
