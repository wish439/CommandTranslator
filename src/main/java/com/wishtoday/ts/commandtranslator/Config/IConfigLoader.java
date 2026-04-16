package com.wishtoday.ts.commandtranslator.Config;

import com.wishtoday.ts.commandtranslator.Config.Adapter.AnnotationAdapter.AnnotationAdapter;
import com.wishtoday.ts.commandtranslator.Config.Adapter.TypeAdapter.FieldTypeAdapter;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface IConfigLoader<T> {
    T load(Supplier<T> supplier, Path path);
    <A extends Annotation> void registerAnnotationAdapter(Class<A> clazz, AnnotationAdapter<A> annotationAdapter);
    <A> void registerFieldTypeAdapter(FieldTypeAdapter<A> fieldTypeAdapter);
}
