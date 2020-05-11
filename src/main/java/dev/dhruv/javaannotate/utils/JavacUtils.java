package dev.dhruv.javaannotate.utils;

import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class JavacUtils {

    public static JCTree.JCClassDecl getClassDeclaration(Trees trees, Element element){
        if (!(element instanceof TypeElement)){
            throw new IllegalArgumentException("For getting class declaration only TypeElement is supported");
        }
        Tree tree = trees.getTree(element);
        if (!(tree instanceof JCTree.JCClassDecl)){
            throw new IllegalArgumentException(String.format("Tree object is not type of %s", JCTree.JCClassDecl.class.getSimpleName()));
        }
        return  (JCTree.JCClassDecl) tree;
    }

    public static JCTree.JCVariableDecl getVariableDeclaration(Trees trees, Element element){
        if (!(element instanceof VariableElement)){
            throw new IllegalArgumentException("For getting class declaration only VariableElement is supported");
        }
        Tree tree = trees.getTree(element);
        if (!(tree instanceof JCTree.JCVariableDecl)){
            throw new IllegalArgumentException(String.format("Tree object is not type of %s", JCTree.JCVariableDecl.class.getSimpleName()));
        }
        return  (JCTree.JCVariableDecl) tree;
    }
}
