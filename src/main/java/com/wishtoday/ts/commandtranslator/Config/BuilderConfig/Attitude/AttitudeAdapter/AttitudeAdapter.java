package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.Attitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.MutableConfigEntry;
import com.wishtoday.ts.commandtranslator.Data.Configs.ConfigFieldInfo;

public interface AttitudeAdapter<T extends Attitude> {
    default void preRead(MutableConfigEntry<?,?> configEntry, CommentedFileConfig config, T attitude) {

    }

    default Object processRead(MutableConfigEntry<?,?> configEntry, CommentedFileConfig config, T attitude, Object read, String configPath) {
        return read;
    }

    default void postWrite(MutableConfigEntry<?,?> configEntry, CommentedFileConfig config, T attitude, ConfigFieldInfo<?> configPath) {

    }
}
