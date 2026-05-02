package com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.RangeAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Entry.MutableConfigEntry;
import com.wishtoday.ts.commandtranslator.Config.ConfigHelper;

public class RangeAttitudeAdapter implements AttitudeAdapter<RangeAttitude<?>>{
    @Override
    public Object processRead(MutableConfigEntry<?, ?> configEntry, CommentedFileConfig config, RangeAttitude<?> attitude, Object read, String configPath) {
        Number number = (Number) read;
        return ConfigHelper.clampNumber(number, attitude.min(), attitude.max(), number.getClass());
    }
}
