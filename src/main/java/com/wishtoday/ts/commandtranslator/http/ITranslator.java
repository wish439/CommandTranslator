package com.wishtoday.ts.commandtranslator.http;

import org.jetbrains.annotations.NotNull;

public interface ITranslator {
    @NotNull
    String translation(String s, String language);
    @NotNull
    String translation(String s);
}
