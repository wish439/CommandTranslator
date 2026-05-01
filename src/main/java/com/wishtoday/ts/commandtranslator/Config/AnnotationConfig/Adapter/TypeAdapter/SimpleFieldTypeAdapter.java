package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Config.ConfigHelper;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ProcessingContext;

public class SimpleFieldTypeAdapter implements FieldTypeAdapter<Object> {
    @Override
    public boolean shouldApply(ProcessingContext context, ConfigFieldInfo<?> field) {
        return ConfigHelper.isSimpleType(field.field().getType());
    }
}
