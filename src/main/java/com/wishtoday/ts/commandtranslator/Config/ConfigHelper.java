package com.wishtoday.ts.commandtranslator.Config;

import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.Range;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ConfigHelper {
    private ConfigHelper() {}

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
