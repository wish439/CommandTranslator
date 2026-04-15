package com.wishtoday.ts.commandtranslator.Helper.Stringer;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stringer {
    public static final Map<Class<?>, Stringable<?>> STRINGABLE_MAP = new ConcurrentHashMap<>();

    static {
        register(new TextStringable());
        register(new MessageFormatStringable());
    }

    public static void register(Stringable<?> s) {
        STRINGABLE_MAP.put(s.stringableClass(), s);
    }

    @Nullable
    public static String toStringFrom(Object object) {
        Class<?> aClass = object.getClass();
        /*if (!aClass.isAssignableFrom(Stringable.class)) {
            return null;
        }*/
        /*if (!aClass.isInstance(object)) {
            return null;
        }*/
        Stringable<?> stringable = STRINGABLE_MAP.get(aClass);
        if (stringable == null) {
            return null;
        }
        return stringable.string(object);
    }
}
