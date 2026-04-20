package com.wishtoday.ts.commandtranslator.Config;

import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter.AnnotationAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter.FieldTypeAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.AnnotationConfigLoader;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.Attitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.AttitudeAdapter;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.BuilderConfigLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigLoaderBuilder {
    private ConfigLoaderBuilder() {
    }

    public static <T> AnnotationConfigLoaderStage<T> annotationConfigLoader() {
        return new Impls<>();
    }
    public static <T> BuilderConfigLoaderStage<T> builderConfigLoader() {
        return new Impls<>();
    }

    public interface AnnotationConfigLoaderStage<T> {
        <A> AnnotationConfigLoaderStage<T> registerFieldTypeAdapter(FieldTypeAdapter<A> fieldTypeAdapter);

        <A extends Annotation> AnnotationConfigLoaderStage<T> registerAnnotationAdapter(Class<A> clazz, AnnotationAdapter<A> annotationAdapter);

        AnnotationConfigLoader<T> buildAnnotationConfigLoader();
    }

    public interface BuilderConfigLoaderStage<T> {
        BuilderConfigLoaderStage<T> registerAttitudeAdapter(Class<? extends Attitude<?>> clazz, AttitudeAdapter<? extends Attitude<?>> attitudeAdapter);

        BuilderConfigLoader<T> buildBuilderConfigLoader();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Impls<T> implements AnnotationConfigLoaderStage<T>, BuilderConfigLoaderStage<T> {

        private final Map<Class<? extends Annotation>, AnnotationAdapter<? extends Annotation>> annotationAdapters = new ConcurrentHashMap<>();
        private final Set<FieldTypeAdapter<?>> fieldTypeAdapters = new TreeSet<>(
                Comparator.<FieldTypeAdapter<?>>comparingInt(FieldTypeAdapter::priority)
                        .thenComparing(a -> a.getClass().getName())
        );
        private final Map<Class<? extends Attitude<?>>, AttitudeAdapter<? extends Attitude<?>>> attitudes = new ConcurrentHashMap<>();

        @Override
        public <A extends Annotation> AnnotationConfigLoaderStage<T> registerAnnotationAdapter(Class<A> clazz, AnnotationAdapter<A> annotationAdapter) {
            if (!clazz.isAssignableFrom(annotationAdapter.getAnnotationClass())) {
                throw new IllegalArgumentException(annotationAdapter.getAnnotationClass() + " is not assignable to " + clazz);
            }
            annotationAdapters.put(annotationAdapter.getAnnotationClass(), annotationAdapter);
            return this;
        }

        @Override
        public BuilderConfigLoaderStage<T> registerAttitudeAdapter(Class<? extends Attitude<?>> clazz, AttitudeAdapter<? extends Attitude<?>> attitudeAdapter) {
            this.attitudes.put(clazz, attitudeAdapter);
            return this;
        }

        @Override
        public BuilderConfigLoader<T> buildBuilderConfigLoader() {
            return new BuilderConfigLoader<>(this.attitudes);
        }

        @Override
        public AnnotationConfigLoader<T> buildAnnotationConfigLoader() {
            return new AnnotationConfigLoader<>(this.annotationAdapters, this.fieldTypeAdapters);
        }

        @Override
        public <A> AnnotationConfigLoaderStage<T> registerFieldTypeAdapter(FieldTypeAdapter<A> fieldTypeAdapter) {
            fieldTypeAdapters.add(fieldTypeAdapter);
            return this;
        }
    }
}
