package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ProcessingContext;

public class NullFieldTypeAdapter implements FieldTypeAdapter<Object>{

    @Override
    public void write(ProcessingContext context, ConfigFieldInfo<?> field, Object newValue) throws ReflectiveOperationException {
        Object object = field.field().getType().getDeclaredConstructor().newInstance();
        field.field().set(field.configClassInfo().instance(), object);
    }

    @Override
    public boolean shouldApply(ProcessingContext context, ConfigFieldInfo<?> field) {
        return field.value() == null;
    }

    @Override
    public int priority() {
        return 0;
    }
}
