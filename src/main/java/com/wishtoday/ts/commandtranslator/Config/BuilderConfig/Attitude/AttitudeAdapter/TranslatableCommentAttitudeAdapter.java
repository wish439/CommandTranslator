package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.MutableConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.MultiLanguageConfig;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;
import com.wishtoday.ts.commandtranslator.Util.ConfigUtils;

public class TranslatableCommentAttitudeAdapter implements AttitudeAdapter<TranslatableCommentAttitude> {
    @Override
    public void postWrite(MutableConfigEntry<?,?> configEntry, CommentedFileConfig config, TranslatableCommentAttitude attitude, ConfigFieldInfo<?> configFieldInfo) {
        Object instance = configFieldInfo.configClassInfo().instance();

        String defaultComment = ConfigUtils.filterUnblank("\n", attitude.getDefaultComment());
        if (!(instance instanceof MultiLanguageConfig multiLanguageConfig)) {
            config.setComment(configFieldInfo.key(), defaultComment);
            return;
        }

        String currentLanguage = multiLanguageConfig.getCurrentLanguage();
        String s = attitude.getComments().get(currentLanguage);
        if (s == null) {
            config.setComment(configFieldInfo.key(), defaultComment);
            return;
        }
        s = ConfigUtils.filterUnblank("\n", s);
        config.setComment(configFieldInfo.key(), s);
    }
}
