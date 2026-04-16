package com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter;

import java.lang.reflect.Field;

public record FieldInfo<T>(String key,
                        Object value,
                        Field field,
                        ClassInfo<T> classInfo) {
    public FieldInfo(String key,
                     Object value,
                     Field field,
                     String name,
                     Object type,
                     Class<T> clazz) {
        this(key, value, field, new ClassInfo<>(name, type, clazz));
    }
}
