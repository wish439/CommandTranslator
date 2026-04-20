package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ProcessingContext;
import com.wishtoday.ts.commandtranslator.Util.ConfigUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class ObjectTypeAdapter implements FieldTypeAdapter<Object> {

    @Override
    public boolean shouldApply(ProcessingContext context, ConfigFieldInfo<?> field) {
        Class<?> type = field.field().getType();
        return !ConfigUtils.isSimpleType(type)
                && !type.isEnum()
                && !Collection.class.isAssignableFrom(type)
                && !Map.class.isAssignableFrom(type);
    }

    @Override
    public Object read(ProcessingContext context, ConfigFieldInfo<Object> configFieldInfo) {
        return configFieldInfo.value();
    }

    @Override
    public void write(ProcessingContext context, ConfigFieldInfo<?> configFieldInfo, Object newValue)
            throws ReflectiveOperationException {
        Field field = configFieldInfo.field();
        Object obj = configFieldInfo.configClassInfo().instance();
        Object value = configFieldInfo.value();

        try {
            context.recursiveLoader().loadFields(context.config(), value, configFieldInfo.key() + ".");
        } catch (Exception e) {
            throw new ReflectiveOperationException(e);
        }
    }

    @Override
    public int priority() {
        return 1000;
    }
}