package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.Comment;
import com.wishtoday.ts.commandtranslator.Data.Configs.AnnotationInfo;
import com.wishtoday.ts.commandtranslator.Util.ConfigUtils;

public class CommentAdapter implements AnnotationAdapter<Comment> {
    @Override
    public boolean apply0(CommentedConfig config, AnnotationInfo<?, Comment> annotationInfo) {
        config.setComment(annotationInfo.field().key(), ConfigUtils.filterUnblank("\n", annotationInfo.annotation().value()));
        return false;
    }

    @Override
    public Class<Comment> getAnnotationClass() {
        return Comment.class;
    }
}
