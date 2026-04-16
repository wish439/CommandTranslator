package com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.wishtoday.ts.commandtranslator.Config.Annotation.Comment;

import java.util.Arrays;
import java.util.List;

public class CommentAdapter implements AnnotationAdapter<Comment> {
    @Override
    public boolean apply0(CommentedConfig config, AnnotationInfo<?, Comment> annotationInfo) {
        config.setComment(annotationInfo.field().key(), filterUnblank("\n", annotationInfo.annotation().value()));
        return false;
    }

    @Override
    public Class<Comment> getAnnotationClass() {
        return Comment.class;
    }

    private static String filterUnblank(String delimiter, String s) {
        String[] split = s.split(delimiter);
        List<String> list = Arrays.stream(split)
                .filter(a -> !a.isBlank())
                .toList();
        return String.join(delimiter, list);
    }
}
