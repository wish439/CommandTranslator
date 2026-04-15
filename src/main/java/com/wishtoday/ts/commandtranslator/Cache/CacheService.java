package com.wishtoday.ts.commandtranslator.Cache;

import org.jetbrains.annotations.Nullable;

public interface CacheService {
    CacheCheckResult checkCache(String originalCommand);

    void put(String original, String translated);

    @Nullable
    String getTranslated(String original);

    boolean containsOriginal(String original);

    boolean containsTranslated(String translated);
}
