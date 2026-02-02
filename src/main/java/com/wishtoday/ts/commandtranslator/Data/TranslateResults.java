package com.wishtoday.ts.commandtranslator.Data;

import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.List;

public class TranslateResults<T> {
    @Delegate
    private TranslateStringResults stringResults;
    @Getter
    private T result;
    public TranslateResults(T result, TranslateStringResults stringResults) {
        this.result = result;
        this.stringResults = stringResults;
    }
    public TranslateResults(T result, List<String> original, List<String> translated) {
        this.result = result;
        this.stringResults = new TranslateStringResults(original, translated);
    }
}
