package com.wishtoday.ts.commandtranslator.Cache;

import org.jetbrains.annotations.Nullable;

public interface DataSaver {
    void save(CacheInstance data);

    @Nullable
    CacheInstance load();
}
