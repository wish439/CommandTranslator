package com.wishtoday.ts.commandtranslator.Data.Configs;

import java.lang.reflect.Field;

public record ConfigFieldInfo<T>(String key,
                                 Object value,
                                 Field field,
                                 ConfigClassInfo<T> configClassInfo) {
    public ConfigFieldInfo(String key,
                           Object value,
                           Field field,
                           String name,
                           Object type,
                           Class<T> clazz) {
        this(key, value, field, new ConfigClassInfo<>(name, type, clazz));
    }
}
