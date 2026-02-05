package com.wishtoday.ts.commandtranslator.Data;

import com.mojang.brigadier.context.StringRange;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.List;

public class TranslateResults<T> {
    @Delegate
    private TranslateStringResults stringResults;
    @Getter
    private T result;
    @Getter
    private StringRange range;
    public TranslateResults(T result, TranslateStringResults stringResults, StringRange range) {
        this.result = result;
        this.stringResults = stringResults;
        this.range = range;
    }
    public TranslateResults(T result, List<String> original, List<String> translated, StringRange range) {
        this.result = result;
        this.stringResults = new TranslateStringResults(original, translated);
        this.range = range;
    }
}
