package net.moznion.arnold.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ArnoldBuilder annotation expresses that it activates the ArnoldBuilder.
 * <p>
 * When this annotation is given, the annotation processor is enabled and the builder classes are
 * automatically generated.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ArnoldBuilder {
}
