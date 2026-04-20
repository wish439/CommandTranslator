package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.PriorityAttitude;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.TreeSet;
import java.util.function.BiConsumer;

@Data
public class MutableConfigEntry<E,T> {

    private volatile Object value;

    private String serializedName;
    private T defaultValue;
    private TreeSet<PriorityAttitude> adapters;
    private List<MutableConfigEntry<?, ?>> children;
    private BiConsumer<E,T> setter;

    public MutableConfigEntry(@Nullable String serializedName
            , @NotNull T defaultValue
            , @Nullable TreeSet<PriorityAttitude> adapters
            , @Nullable List<MutableConfigEntry<?, ?>> children
            , @Nullable BiConsumer<E,T> setter) {
        this.value = null;
        this.serializedName = serializedName;
        this.defaultValue = defaultValue;
        this.adapters = adapters;
        this.children = children;
        this.setter = setter;
    }

    public T getValue() {
        return (T) value;
    }

    public boolean applySetter(Object e, Object t) {
        if (this.setter == null) return false;
        try {
            this.setter.accept((E) e, (T) t);
            return true;
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /*private MutableConfigEntry(Builder<T> builder) {
        this.serializedName = builder.serializedName;
        this.defaultValue = builder.defaultValue;
        this.adapters = builder.adapters;
        this.children = builder.children;
    }*/

    /*public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private String serializedName;
        private T defaultValue;
        private TreeSet<Attitude<?>> adapters;
        private List<MutableConfigEntry<?>> children;

        public Builder() {
            this.serializedName = null;
            this.defaultValue = null;
            this.adapters = new TreeSet<>(
                    Comparator.<Attitude<?>>comparingInt(Attitude::priority)
                            .thenComparing(a -> a.getClass().getName())
            );
            this.children = new ArrayList<>();
        }

        public Builder<T> serializedName(String name) {
            this.serializedName = name;
            return this;
        }

        public Builder<T> defaultValue(T value) {
            this.defaultValue = value;
            return this;
        }

        public Builder<T> adapter(Attitude<?> attitude) {
            this.adapters.add(attitude);
            return this;
        }

        public Builder<T> child(MutableConfigEntry<?> entry) {
            this.children.add(entry);
            return this;
        }

        public MutableConfigEntry<T> buildAnnotationConfigLoader() {
            return new MutableConfigEntry<>(this);
        }
    }*/
}
