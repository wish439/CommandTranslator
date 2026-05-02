package com.wishtoday.ts.commandtranslator.CommandHandler;

import com.mojang.brigadier.context.StringRange;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Helper.Stringer.Stringer;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;
import com.wishtoday.ts.commandtranslator.Util.LanguageUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TranslateHelper {
     static Function<String, String> getDefaultTranslateStrategy(Config config, BatchTranslatorProcessor processor) {
        return s -> {
            if (LanguageUtils.isChineseSentence(s, config.getChineseSentenceJudgmentRange())) return s;
            if (LanguageUtils.isOnlySymbols(s)) return s;
            if (s.isBlank()) return s;
            try {
                String join = processor
                        .submit(s)
                        .completeOnTimeout(s, 15, TimeUnit.SECONDS)
                        .join();
                return join;
            } catch (CompletionException e) {
                Commandtranslator.LOGGER.error("Translate failed for '{}'", s, e.getCause());
                return s;
            } catch (Exception e) {
                Commandtranslator.LOGGER.error("Translate unexpected error for '{}'", s, e);
                return s;
            }
        };
    }

    static Function<String, CompletableFuture<String>> getDefaultAsyncTranslateStrategy(Config config, BatchTranslatorProcessor processor) {
        return s -> {
            if (LanguageUtils.isChineseSentence(s, config.getChineseSentenceJudgmentRange())) return CompletableFuture.completedFuture(s);
            if (LanguageUtils.isOnlySymbols(s)) return CompletableFuture.completedFuture(s);
            //if (s.isBlank()) return CompletableFuture.completedFuture(s);
            return processor.submit(s);
        };
    }

    static <T> String getReplacedCommand(String original, StringRange range, T result) {
        String before = original.substring(0, range.getStart());
        String after = original.substring(range.getEnd());
        String middle = Stringer.toStringFrom(result);
        return before + middle + after;
    }
}
