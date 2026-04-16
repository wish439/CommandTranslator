package com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public record AnnotationInfo<T, A extends Annotation>(FieldInfo<T> field, A annotation) {
    public AnnotationInfo(String key, Object value, Field field, ClassInfo<T> classInfo, A annotation) {
        this(new FieldInfo<>(key, value, field, classInfo), annotation);
    }
}
