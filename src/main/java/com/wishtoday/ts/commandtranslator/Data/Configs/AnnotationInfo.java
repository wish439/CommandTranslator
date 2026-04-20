package com.wishtoday.ts.commandtranslator.Data.Configs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public record AnnotationInfo<T, A extends Annotation>(ConfigFieldInfo<T> field, A annotation) {
    public AnnotationInfo(String key, Object value, Field field, ConfigClassInfo<T> configClassInfo, A annotation) {
        this(new ConfigFieldInfo<>(key, value, field, configClassInfo), annotation);
    }
}
