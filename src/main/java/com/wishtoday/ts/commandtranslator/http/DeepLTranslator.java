package com.wishtoday.ts.commandtranslator.http;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLException;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import org.jetbrains.annotations.NotNull;

public class DeepLTranslator extends AbstractTranslator{
    private DeepLClient deepLClient;

    public DeepLTranslator(String api, String key) {
        super(api, key);
        this.deepLClient = new DeepLClient(key);
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
}
