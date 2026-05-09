package com.wishtoday.ts.commandtranslator.Data;

import com.mojang.brigadier.context.StringRange;
import lombok.Getter;

public class TranslateResults<T> {
    @Getter
    private final T result;
    @Getter
    private final StringRange range;
    public TranslateResults(T result, StringRange range) {
        this.result = result;
        this.range = range;
    }
}
