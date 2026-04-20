package com.wishtoday.ts.commandtranslator.Data.Configs;

public record ConfigClassInfo<T>(String name, Object instance, Class<T> clazz) {
    public ConfigClassInfo(Object value) {
        this(value.getClass().getName(), value, (Class<T>) value.getClass());
    }
}
