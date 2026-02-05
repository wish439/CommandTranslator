package com.wishtoday.ts.commandtranslator.CommandTranslator;

import com.mojang.brigadier.context.StringRange;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ArgumentTranslator<T> {
    @Nullable
    TranslateResults<T> translate(T value, StringRange range, Function<String, String> o2nFunction);
}
