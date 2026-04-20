package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ProcessingContext;

public class EnumFieldTypeAdapter implements FieldTypeAdapter<Enum<?>> {


    @Override
    public void write(ProcessingContext context, ConfigFieldInfo<?> field, Object newValue) throws ReflectiveOperationException {
        Enum value = Enum.valueOf((Class<Enum>) field.field().getType(), newValue.toString());
        field.field().set(field.configClassInfo().instance(), value);
    }

    @Override
    public boolean shouldApply(ProcessingContext context, ConfigFieldInfo<?> field) {
        return field.field().getType().isEnum();
    }
}
