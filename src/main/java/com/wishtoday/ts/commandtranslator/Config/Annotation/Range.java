package com.wishtoday.ts.commandtranslator.Config.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    int minInt() default Integer.MIN_VALUE;
    int maxInt() default Integer.MAX_VALUE;
    double minDouble() default Double.MIN_VALUE;
    double maxDouble() default Double.MAX_VALUE;
    double minFloat() default Float.MIN_VALUE;
    double maxFloat() default Float.MAX_VALUE;
    double minLong() default Long.MIN_VALUE;
    double maxLong() default Long.MAX_VALUE;
}
