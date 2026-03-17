package com.wishtoday.ts.commandtranslator.Util;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TranslateUtils {
    public static Function<String, String> getDefaultTranslateStrategy(Config config, BatchTranslatorProcessor processor) {
        return s -> {
            if (LanguageUtils.isChineseSentence(s, config.getChineseSentenceJudgmentRange())) return s;
            try {
                return processor
                        .submit(s)
                        .completeOnTimeout(s, 4, TimeUnit.SECONDS)
                        .join();
            } catch (CompletionException e) {
                Commandtranslator.LOGGER.error("Translate failed for '{}'", s, e.getCause());
                return s;
            } catch (Exception e) {
                Commandtranslator.LOGGER.error("Translate unexpected error for '{}'", s, e);
                return s;
            }
        };
    }

    public static Function<String, CompletableFuture<String>> getDefaultAsyncTranslateStrategy(Config config, BatchTranslatorProcessor processor) {
        return s -> {
            if (LanguageUtils.isChineseSentence(s, config.getChineseSentenceJudgmentRange())) return CompletableFuture.completedFuture(s);
            return processor.submit(s);
        };
    }
}
