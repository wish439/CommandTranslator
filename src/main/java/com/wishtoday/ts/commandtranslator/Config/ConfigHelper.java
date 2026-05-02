package com.wishtoday.ts.commandtranslator.Config;

import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.Range;

public final class ConfigHelper {
    private ConfigHelper() {}

    public static Number clampNumber(Number value, Number min, Number max, Class<?> type) {

        if (type == int.class || type == Integer.class) {
            int v = value.intValue();
            return Math.min(Math.max(min.intValue(), v), max.intValue());
        }
        if (type == long.class || type == Long.class) {
            long v = value.longValue();
            return Math.min(Math.max(min.longValue(), v), max.longValue());
        }
        if (type == float.class || type == Float.class) {
            float v = value.floatValue();
            return Math.min(Math.max(min.floatValue(), v), max.floatValue());
        }
        if (type == double.class || type == Double.class) {
            double v = value.doubleValue();
            return Math.min(Math.max(min.doubleValue(), v), max.doubleValue());
        }
        return value;
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
}
