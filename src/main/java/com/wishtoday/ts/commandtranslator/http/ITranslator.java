package com.wishtoday.ts.commandtranslator.http;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ITranslator {
    @NotNull
    String translation(String s, String language);
    @NotNull
    String translation(String s);
    List<String> translate(List<String> strings);
}
