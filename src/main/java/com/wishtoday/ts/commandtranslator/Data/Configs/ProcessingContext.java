package com.wishtoday.ts.commandtranslator.Data.Configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.wishtoday.ts.commandtranslator.Config.RecursiveLoadCallback;

public record ProcessingContext(CommentedFileConfig config, RecursiveLoadCallback recursiveLoader) {
}