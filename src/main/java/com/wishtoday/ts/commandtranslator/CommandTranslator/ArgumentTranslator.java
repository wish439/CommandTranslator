package com.wishtoday.ts.commandtranslator.CommandTranslator;

import com.mojang.brigadier.context.StringRange;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ArgumentTranslator<T> {
    @Nullable
    TranslateResults<T> translate(T value, StringRange range, Function<String, String> o2nFunction);

    @NotNull
    CompletableFuture<TranslateResults<T>> translateAsync(T value, StringRange range, Function<String, CompletableFuture<String>> o2nFunction);

}
