package com.wishtoday.ts.commandtranslator.Util;

import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.Range;

import java.util.Arrays;
import java.util.List;

public final class ConfigUtils {
    private ConfigUtils() {}

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
                || List.class.isAssignableFrom(type);
    }

    public static Number clampNumber(Number value, Range range, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            int v = value.intValue();
            return Math.min(Math.max(range.minInt(), v), range.maxInt());
        }
        if (type == long.class || type == Long.class) {
            long v = value.longValue();
            return Math.min(Math.max(range.minLong(), v), range.maxLong());
        }
        if (type == float.class || type == Float.class) {
            float v = value.floatValue();
            return Math.min(Math.max(range.minFloat(), v), range.maxFloat());
        }
        if (type == double.class || type == Double.class) {
            double v = value.doubleValue();
            return Math.min(Math.max(range.minDouble(), v), range.maxDouble());
        }
        return value;
    }

    public static String filterUnblank(String delimiter, String s) {
        String[] split = s.split(delimiter);
        List<String> list = Arrays.stream(split)
                .filter(a -> !a.isBlank())
                .toList();
        return String.join(delimiter, list);
    }
}
