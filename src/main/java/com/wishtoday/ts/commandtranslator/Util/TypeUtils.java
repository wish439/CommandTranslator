package com.wishtoday.ts.commandtranslator.Util;

import java.util.List;
import java.util.Map;

public class TypeUtils {
    private TypeUtils() {}
    public static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Short.class
                || type == Byte.class
                || type == Character.class
                || List.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type);
    }
}
