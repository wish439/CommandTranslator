package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.Range;
import com.wishtoday.ts.commandtranslator.Data.Configs.AnnotationInfo;
import com.wishtoday.ts.commandtranslator.Util.ConfigUtils;

import java.lang.reflect.Field;

public class RangeAdapter implements AnnotationAdapter<Range> {
    /*@Override
    public boolean apply0(CommentedConfig config, AnnotationInfo<?, Range> annotationInfo) throws IllegalAccessException {
        Field field = annotationInfo.field().field();
        Class<?> instance = field.getType();
        if (!ConfigUtils.isSimpleType(instance)) {
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
            read = ConfigUtils.clampNumber(n, range, instance);
        }

        if (instance.isEnum() && read != null) {
            read = Enum.valueOf((Class<Enum>) instance, read.toString());
        }

        Object obj = annotationInfo.field().configClassInfo().instance();

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
        Object obj = annotationInfo.field().configClassInfo().instance();

        field.set(obj, read);
        return true;
    }

    @Override
    public Class<Range> getAnnotationClass() {
        return Range.class;
    }
}
