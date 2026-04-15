package com.wishtoday.ts.commandtranslator.Helper.Stringer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Stringable<T> {
    String stringValue(T value);

    @NotNull Class<T> stringableClass();

    @Nullable
    default String string(Object value) {
        if (!stringableClass().isInstance(value)) {
            return null;
        }
        return stringValue((T) value);
    }
}
