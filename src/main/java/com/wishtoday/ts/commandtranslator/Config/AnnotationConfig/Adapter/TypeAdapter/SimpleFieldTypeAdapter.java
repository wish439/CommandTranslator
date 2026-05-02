package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Data.Configs.ProcessingContext;
import com.wishtoday.ts.commandtranslator.Util.TypeUtils;

public class SimpleFieldTypeAdapter implements FieldTypeAdapter<Object> {
    @Override
    public boolean shouldApply(ProcessingContext context, ConfigFieldInfo<?> field) {
        return TypeUtils.isSimpleType(field.field().getType());
    }
}
