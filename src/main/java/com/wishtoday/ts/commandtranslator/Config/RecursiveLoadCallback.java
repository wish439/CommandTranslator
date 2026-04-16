package com.wishtoday.ts.commandtranslator.Config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

@FunctionalInterface
public interface RecursiveLoadCallback {
    void loadFields(CommentedFileConfig config, Object obj, String prefix) throws Exception;
}