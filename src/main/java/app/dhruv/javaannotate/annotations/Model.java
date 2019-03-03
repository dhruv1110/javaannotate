package app.dhruv.javaannotate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface Model {
    String value();

    boolean allArgConstructor() default true;

    boolean emptyConstructor() default true;

    boolean getters() default true;

    boolean setters() default true;

    boolean builder() default false;

    boolean singleton() default false;

    boolean equalsMethod() default false;

    boolean hashCodeMethod() default false;

    boolean toStringMethod() default false;
}
