package dev.dhruv.javaannotate.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.List;

public class ElementUtils {

    public static List<VariableElement> getFields(Element element){
        if (element.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException("Fields can be only retrieved from class(TypeElement)");
        }

        TypeElement typeElement = (TypeElement) element;

        return ElementFilter.fieldsIn(typeElement.getEnclosedElements());
    }

    public static String getterMathodName(VariableElement variableElement){
        String variableName = variableElement.getSimpleName().toString();
        return String.format("get%s", capitalizeName(variableName));
    }

    public static String setterMathodName(VariableElement variableElement){
        String variableName = variableElement.getSimpleName().toString();
        return String.format("set%s", capitalizeName(variableName));
    }

    private static String capitalizeName(String name){
        return String.format("%s%s", name.substring(0, 1).toUpperCase(), name.substring(1));
    }
}
