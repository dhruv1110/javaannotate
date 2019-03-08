package dev.dhruv.javaannotate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface Field {
    boolean getter() default true;

    String renameTo() default "";

    boolean setter() default true;

    /**
     * represents the class name for which this field configuration will apply
     *
     * @return class name that will be created
     */
    String value();
}
