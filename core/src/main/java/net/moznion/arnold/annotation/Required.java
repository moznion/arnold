package net.moznion.arnold.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required annotation indicates that the given parameter is mandatory for the ArnoldBuilder.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
    /**
     * order parameter can specify the order of appearance the fields in the builder.
     * <p>
     * Lower values are processed first. If the same value is given, the one defined earlier will be
     * processed first.
     *
     * @return order of appearance the fields in the builder
     */
    int order() default -1;
}
