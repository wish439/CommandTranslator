package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.TranslatableComment;
import com.wishtoday.ts.commandtranslator.Data.Configs.AnnotationInfo;
import com.wishtoday.ts.commandtranslator.Config.MultiLanguageConfig;
import com.wishtoday.ts.commandtranslator.Util.StringUtils;

public class TranslatableCommentAdapter implements AnnotationAdapter<TranslatableComment>{
    @Override
    public boolean apply0(CommentedConfig config, AnnotationInfo<?, TranslatableComment> annotationInfo) throws ReflectiveOperationException {
        TranslatableComment comment = annotationInfo.annotation();
        Object instance = annotationInfo.field().configClassInfo().instance();
        Object o = instance instanceof MultiLanguageConfig m ? m.getCurrentLanguage() : (config.get("configLang") == null ? null : config.get("configLang"));
        if (o == null) {
            if (!comment.defaultValue().isEmpty()) {
                config.setComment(annotationInfo.field().key(), StringUtils.filterUnblank("\n" ,comment.defaultValue()));
            }
            if (comment.defaultValueIndexInValue() != -1) {
                config.setComment(annotationInfo.field().key(), StringUtils.filterUnblank("\n", comment.value()[comment.defaultValueIndexInValue()]));
            }
            return false;
        }
        String s = (String) o;
        for (int i = 0; i < comment.language().length; i++) {
            if (!s.equalsIgnoreCase(comment.language()[i])) {
                continue;
            }
            String content = comment.value()[i];
            config.setComment(annotationInfo.field().key(), StringUtils.filterUnblank("\n" ,content));
            return false;
        }
        return false;
    }

    @Override
    public Class<TranslatableComment> getAnnotationClass() {
        return TranslatableComment.class;
    }
}
