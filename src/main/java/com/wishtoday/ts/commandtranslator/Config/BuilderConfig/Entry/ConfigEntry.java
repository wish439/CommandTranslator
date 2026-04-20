package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.Attitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.PriorityAttitude;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public record ConfigEntry<E, T>(MutableConfigEntry<E, T> mutableConfigEntry) {

    private ConfigEntry(Builder<E, T> builder) {
        this(new MutableConfigEntry<>(builder.serializedName, builder.defaultValue, builder.adapters, builder.children.stream().map(ConfigEntry::toMutable).collect(Collectors.toList()), builder.setter));
    }

    public MutableConfigEntry<E, T> toMutable() {
        return this.mutableConfigEntry;
    }

    private static ConfigEntry<?, ?> newConfigEntry(MutableConfigEntry<?, ?> configEntry) {
        return new ConfigEntry<>(configEntry);
    }

    public T getValue() {
        return mutableConfigEntry.getValue();
    }

    public String getSerializedName() {
        return mutableConfigEntry.getSerializedName();
    }

    public T getDefaultValue() {
        return mutableConfigEntry.getDefaultValue();
    }

    public SortedSet<PriorityAttitude> getAttitudes() {
        return Collections.unmodifiableSortedSet(mutableConfigEntry.getAdapters());
    }

    public List<ConfigEntry<?, ?>> getChildren() {
        return mutableConfigEntry.getChildren()
                .stream()
                .<ConfigEntry<?, ?>>map(ConfigEntry::newConfigEntry)
                .toList();
    }

    public static <E, T> MustSetStage<E, T> builder() {
        return new Builder<>();
    }

    public interface MustSetStage<E, T> {
        MustSetStage<E, T> serializedName(@NotNull String name);

        OtherSetStage<E, T> defaultValue(@NotNull T value);
    }

    public interface OtherSetStage<E, T> {
        OtherSetStage<E, T> adapter(Attitude<?> attitude);

        OtherSetStage<E, T> adapter(Attitude<?> attitude, int priority);

        OtherSetStage<E, T> child(ConfigEntry<?, ?> entry);

        OtherSetStage<E, T> setter(BiConsumer<E, T> setter);

        ConfigEntry<E, T> build();
    }

    public static class Builder<E, T> implements MustSetStage<E, T>, OtherSetStage<E, T> {
        private String serializedName;
        private T defaultValue;
        private TreeSet<PriorityAttitude> adapters;
        private List<ConfigEntry<?, ?>> children;
        private BiConsumer<E, T> setter;

        public Builder() {
            this.serializedName = null;
            this.defaultValue = null;
            this.adapters = new TreeSet<>(
                    Comparator.comparingInt(PriorityAttitude::priority)
                            .thenComparing(a -> a.getClass().getName())
            );
            this.children = new ArrayList<>();
            this.setter = null;
        }

        public MustSetStage<E, T> serializedName(@NotNull String name) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("serializedName cannot be empty");
            }
            this.serializedName = name;
            return this;
        }

        public OtherSetStage<E, T> defaultValue(@NotNull T value) {
            this.defaultValue = value;
            return this;
        }

        public OtherSetStage<E, T> adapter(Attitude<?> attitude) {
            this.adapters.add(new PriorityAttitude(attitude, 1));
            return this;
        }

        public OtherSetStage<E, T> adapter(Attitude<?> attitude, int priority) {
            this.adapters.add(new PriorityAttitude(attitude, priority));
            return this;
        }

        public OtherSetStage<E, T> child(ConfigEntry<?, ?> entry) {
            this.children.add(entry);
            return this;
        }

        public OtherSetStage<E, T> setter(BiConsumer<E, T> setter) {
            this.setter = setter;
            return this;
        }

        public ConfigEntry<E, T> build() {
            return new ConfigEntry<>(this);
        }
    }
}
