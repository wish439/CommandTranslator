package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.DisplayControlAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.MutableConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.MultiLanguageConfig;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;

public class DisplayControlAttitudeAdapter implements AttitudeAdapter<DisplayControlAttitude>{
    @Override
    public void postWrite(MutableConfigEntry<?, ?> configEntry, CommentedFileConfig config, DisplayControlAttitude attitude, ConfigFieldInfo<?> configPath) {
        Object instance = configPath.configClassInfo().instance();
        if (!(instance instanceof MultiLanguageConfig multiLanguageConfig)) {
            return;
        }
        String language = multiLanguageConfig.getCurrentLanguage();
        if (!language.equalsIgnoreCase(attitude.onlyDisplay())) {
            config.remove(configPath.key());
            config.removeComment(configPath.key());
            return;
        }
        if (attitude.notDisplay().contains(language)) {
            config.remove(configPath.key());
            config.removeComment(configPath.key());
        }
    }
}
