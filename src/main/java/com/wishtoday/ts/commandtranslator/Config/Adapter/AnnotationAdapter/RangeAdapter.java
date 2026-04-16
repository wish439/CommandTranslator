package com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.wishtoday.ts.commandtranslator.Config.Annotation.Range;
import com.wishtoday.ts.commandtranslator.Util.ConfigUtils;

import java.lang.reflect.Field;

public class RangeAdapter implements AnnotationAdapter<Range> {
    /*@Override
    public boolean apply0(CommentedConfig config, AnnotationInfo<?, Range> annotationInfo) throws IllegalAccessException {
        Field field = annotationInfo.field().field();
        Class<?> type = field.getType();
        if (!ConfigUtils.isSimpleType(type)) {
            return false;
        }
        Object value = annotationInfo.field().value();
        String key = annotationInfo.field().key();
        if (!config.contains(key)) {
            config.set(key, value);
        }

        Object read = config.get(key);

        Range range = annotationInfo.annotation();
        if (range != null && read instanceof Number n) {
            read = ConfigUtils.clampNumber(n, range, type);
        }

        if (type.isEnum() && read != null) {
            read = Enum.valueOf((Class<Enum>) type, read.toString());
        }

        Object obj = annotationInfo.field().classInfo().type();

        field.set(obj, read);
        return true;
    }*/

    @Override
    public boolean apply0(CommentedConfig config, AnnotationInfo<?, Range> annotationInfo) throws ReflectiveOperationException {
        Range range = annotationInfo.annotation();
        Object read = annotationInfo.field().value();
        Field field = annotationInfo.field().field();
        Class<?> type = field.getType();
        if (range != null && read instanceof Number n) {
            read = ConfigUtils.clampNumber(n, range, type);
        }
        Object obj = annotationInfo.field().classInfo().type();

        field.set(obj, read);
        return true;
    }

    @Override
    public Class<Range> getAnnotationClass() {
        return Range.class;
    }
}
