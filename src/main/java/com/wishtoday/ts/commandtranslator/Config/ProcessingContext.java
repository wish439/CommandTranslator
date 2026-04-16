package com.wishtoday.ts.commandtranslator.Config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public record ProcessingContext(CommentedFileConfig config, RecursiveLoadCallback recursiveLoader) {
}