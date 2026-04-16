package com.wishtoday.ts.commandtranslator.Config.Adapter.TypeAdapter;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.FieldInfo;
import com.wishtoday.ts.commandtranslator.Config.ProcessingContext;
import com.wishtoday.ts.commandtranslator.Util.ConfigUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class ObjectTypeAdapter implements FieldTypeAdapter<Object> {

    @Override
    public boolean shouldApply(ProcessingContext context, FieldInfo<?> field) {
        Class<?> type = field.field().getType();
        return !ConfigUtils.isSimpleType(type)
                && !type.isEnum()
                && !Collection.class.isAssignableFrom(type)
                && !Map.class.isAssignableFrom(type);
    }

    @Override
    public Object read(ProcessingContext context, FieldInfo<Object> fieldInfo) {
        return fieldInfo.value();
    }

    @Override
    public void write(ProcessingContext context, FieldInfo<?> fieldInfo, Object newValue)
            throws ReflectiveOperationException {
        Field field = fieldInfo.field();
        Object obj = fieldInfo.classInfo().type();
        Object value = fieldInfo.value();

        if (value == null) {
            value = field.getType().getDeclaredConstructor().newInstance();
            field.set(obj, value);
        }

        try {
            context.recursiveLoader().loadFields(context.config(), value, fieldInfo.key() + ".");
        } catch (Exception e) {
            throw new ReflectiveOperationException(e);
        }
    }

    @Override
    public int priority() {
        return 1000;
    }
}