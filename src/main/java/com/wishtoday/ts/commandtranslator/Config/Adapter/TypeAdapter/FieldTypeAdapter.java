package com.wishtoday.ts.commandtranslator.Config.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.FieldInfo;
import com.wishtoday.ts.commandtranslator.Config.ProcessingContext;

public interface FieldTypeAdapter<T> {
    default T read(ProcessingContext context, FieldInfo<T> field) {
        return context.config().get(field.key());
    }
    default void write(ProcessingContext context, FieldInfo<?> field, Object newValue) throws ReflectiveOperationException {
        field.field().set(field.classInfo().type(), newValue);
    }
    boolean shouldApply(ProcessingContext context, FieldInfo<?> field);
    default int priority() {
        return 1;
    };
}
