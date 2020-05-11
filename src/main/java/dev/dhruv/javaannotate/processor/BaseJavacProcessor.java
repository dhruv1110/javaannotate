package dev.dhruv.javaannotate.processor;

import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;

public abstract class BaseJavacProcessor extends AbstractProcessor {

    Elements elements;
    JavacElements javacElements;
    Messager messager;
    TreeMaker treeMaker;
    Trees trees;
    JavacTypes types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        this.trees = Trees.instance(processingEnv);
        Context ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(ctx);
        this.javacElements = JavacElements.instance(ctx);
        this.processingEnv = processingEnv;
        types = (JavacTypes) processingEnv.getTypeUtils();

    }

}
