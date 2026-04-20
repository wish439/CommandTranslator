package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ProcessingContext;

public interface FieldTypeAdapter<T> {
    default T read(ProcessingContext context, ConfigFieldInfo<T> field) {
        return context.config().get(field.key());
    }
    default void write(ProcessingContext context, ConfigFieldInfo<?> field, Object newValue) throws ReflectiveOperationException {
        field.field().set(field.configClassInfo().instance(), newValue);
    }
    boolean shouldApply(ProcessingContext context, ConfigFieldInfo<?> field);
    default int priority() {
        return 1;
    };
}
