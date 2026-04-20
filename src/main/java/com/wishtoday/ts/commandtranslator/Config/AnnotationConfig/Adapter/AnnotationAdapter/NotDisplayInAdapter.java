package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter;


import com.electronwill.nightconfig.core.CommentedConfig;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.NotDisplayIn;
import com.wishtoday.ts.commandtranslator.Data.Configs.AnnotationInfo;
import com.wishtoday.ts.commandtranslator.Config.MultiLanguageConfig;

public class NotDisplayInAdapter implements AnnotationAdapter<NotDisplayIn> {
    @Override
    public boolean apply0(CommentedConfig config, AnnotationInfo<?, NotDisplayIn> annotationInfo) throws ReflectiveOperationException {
        NotDisplayIn annotation = annotationInfo.annotation();
        Object instance = annotationInfo.field().configClassInfo().instance();
        if (!(instance instanceof MultiLanguageConfig multiLanguageConfig)) return false;
        String currentLanguage = multiLanguageConfig.getCurrentLanguage();
        if (!annotation.value().equalsIgnoreCase(currentLanguage)) return false;
        config.remove(annotationInfo.field().key());
        config.removeComment(annotationInfo.field().key());
        return false;
    }

    @Override
    public Class<NotDisplayIn> getAnnotationClass() {
        return NotDisplayIn.class;
    }
}
