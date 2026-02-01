package com.wishtoday.ts.commandtranslator.Data;

import lombok.Getter;

import java.util.List;

@Getter
public class TranslateStringResults {
    private final List<String> original;
    private final List<String> translated;
    public TranslateStringResults(List<String> original, List<String> translated) {
        this.original = original;
        this.translated = translated;
    }
    public String[] original() {
        return original.toArray(new String[0]);
    }
    public String[] translated() {
        return translated.toArray(new String[0]);
    }
}
