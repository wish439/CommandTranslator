package com.wishtoday.ts.commandtranslator.Config.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.FieldInfo;
import com.wishtoday.ts.commandtranslator.Config.ProcessingContext;

public class NullFieldTypeAdapter implements FieldTypeAdapter<Object>{

    @Override
    public void write(ProcessingContext context, FieldInfo<?> field, Object newValue) throws ReflectiveOperationException {
        Object object = field.field().getType().getDeclaredConstructor().newInstance();
        field.field().set(field.classInfo().type(), object);
    }

    @Override
    public boolean shouldApply(ProcessingContext context, FieldInfo<?> field) {
        return field.value() == null;
    }

    @Override
    public int priority() {
        return FieldTypeAdapter.super.priority();
    }
}
