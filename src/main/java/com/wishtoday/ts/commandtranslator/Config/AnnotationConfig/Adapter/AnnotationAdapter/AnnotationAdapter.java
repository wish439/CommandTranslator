package com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.wishtoday.ts.commandtranslator.Data.Configs.AnnotationInfo;

import java.lang.annotation.Annotation;

public interface AnnotationAdapter<T extends Annotation> {
    boolean apply0(CommentedConfig config, AnnotationInfo<?, T> annotationInfo) throws ReflectiveOperationException;

    Class<T> getAnnotationClass();

    default boolean apply(CommentedConfig config, AnnotationInfo<?, ? extends Annotation> annotationInfo) throws ReflectiveOperationException {
        if (!this.getAnnotationClass().isInstance(annotationInfo.annotation())) {
            return false;
        }
        T t = getAnnotationClass().cast(annotationInfo.annotation());
        return apply0(config, new AnnotationInfo<>(annotationInfo.field().key(), annotationInfo.field().value(), annotationInfo.field().field(), annotationInfo.field().configClassInfo(), t));
    }
}
