package com.wishtoday.ts.commandtranslator.Util;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class TranslateUtils {
    public static Function<String, String> getDefaultTranslateStrategy(Config config, BatchTranslatorProcessor processor) {
        return s -> {
            if (LanguageUtils.isChineseSentence(s, config.getChineseSentenceJudgmentRange())) return s;

            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return processor.submit(s).get(3, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    Commandtranslator.LOGGER.error(e.getMessage());
                    return s;
                }
            });
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                Commandtranslator.LOGGER.error(e.getMessage());
                return s;
            }
        };
    }
}
