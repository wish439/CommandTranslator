package com.wishtoday.ts.commandtranslator.Config;

import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import org.jetbrains.annotations.NotNull;

public interface ApplicationConfig {
    boolean canWorkOn(TranslateEnvironment environment);
    boolean validateCommand(@NotNull String command);
}
