package com.wishtoday.ts.commandtranslator.http;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLException;
import com.deepl.api.TextResult;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DeepLTranslator implements ITranslators {
    private DeepLClient deepLClient;
    private final ValidateInfo validate;

    public DeepLTranslator(String api, String key) {
        this.deepLClient = new DeepLClient(key);
        this.validate = new ValidateInfo(api, key);
    }

    @NotNull
    @Override
    public String translation(String s, String language) {
        try {
            return deepLClient.translateText(s, null, language).getText();
        } catch (DeepLException | InterruptedException e) {
            Commandtranslator.LOGGER.error("deepLTranslator error", e);
            return s;
        }
    }

    @NotNull
    @Override
    public String translation(String s) {
        return this.translation(s, "zh");
    }

    @Override
    public List<String> translate(List<String> strings) {
        try {
            return deepLClient.translateText(strings, null, "zh").stream().map(TextResult::getText).collect(Collectors.toList());
        } catch (DeepLException | InterruptedException e) {
            Commandtranslator.LOGGER.error("deepLTranslator error", e);
            return strings;
        }
    }
}
