package com.wishtoday.ts.commandtranslator.Translator;

import com.wishtoday.ts.commandtranslator.http.DeepLTranslator;
import com.wishtoday.ts.commandtranslator.http.ITranslator;
import com.wishtoday.ts.commandtranslator.http.OpenAITranslator;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public class TranslatorFactory {
    @NotNull
    private String api;
    @NotNull
    private String key;
    @NotNull
    private String model;
    private int contextNumber;

    public ITranslator getTranslator(@NotNull TranslatorType type) {
        return switch (type) {
            case OPENAI -> new OpenAITranslator(api, key, model, contextNumber);
            case DEEPL -> new DeepLTranslator(api, key);
        };
    }
}
