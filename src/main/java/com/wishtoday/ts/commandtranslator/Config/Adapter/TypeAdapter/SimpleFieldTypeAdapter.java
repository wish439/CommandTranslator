package com.wishtoday.ts.commandtranslator.Config.Adapter.TypeAdapter;

import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.FieldInfo;
import com.wishtoday.ts.commandtranslator.Config.ProcessingContext;
import com.wishtoday.ts.commandtranslator.Util.ConfigUtils;

public class SimpleFieldTypeAdapter implements FieldTypeAdapter<Object> {
    @Override
    public boolean shouldApply(ProcessingContext context, FieldInfo<?> field) {
        return ConfigUtils.isSimpleType(field.field().getType());
    }
}
