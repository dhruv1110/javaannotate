package app.dhruv.javaannotate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface FieldOf {
    String value();

    boolean getter() default true;

    boolean setter() default true;

    String fieldName() default "";

}
