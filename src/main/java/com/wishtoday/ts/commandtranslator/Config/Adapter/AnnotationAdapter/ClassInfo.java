package com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter;

public record ClassInfo<T>(String name, Object type, Class<T> clazz) {
    public ClassInfo(Object value) {
        this(value.getClass().getName(), value, (Class<T>) value.getClass());
    }
}
