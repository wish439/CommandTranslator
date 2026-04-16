package com.wishtoday.ts.commandtranslator.Config.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.FieldInfo;
import com.wishtoday.ts.commandtranslator.Config.ProcessingContext;

public class EnumFieldTypeAdapter implements FieldTypeAdapter<Enum<?>> {


    @Override
    public void write(ProcessingContext context, FieldInfo<?> field, Object newValue) throws ReflectiveOperationException {
        Enum value = Enum.valueOf((Class<Enum>) field.field().getType(), newValue.toString());
        field.field().set(field.classInfo().type(), value);
    }

    @Override
    public boolean shouldApply(ProcessingContext context, FieldInfo<?>  field) {
        return field.field().getType().isEnum();
    }
}
