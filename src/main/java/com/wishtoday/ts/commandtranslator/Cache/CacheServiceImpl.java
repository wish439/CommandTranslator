package com.wishtoday.ts.commandtranslator.Cache;

import org.jetbrains.annotations.Nullable;

public record CacheServiceImpl(CacheInstance instance) implements CacheService {

    @Override
    public CacheInstance getCacheInstance() {
        return instance;
    }

    @Override
    public CacheCheckResult checkCache(String originalCommand) {
        ConcurrentBiHashMap<String, String> t = instance.getAllCommando2t();
        if (t.containsValue(originalCommand)) {
            return CacheCheckResult.alreadyExists();
        }
        String value = t.getValue(originalCommand);
        if (value == null) return CacheCheckResult.missing();
        return CacheCheckResult.success(value);
    }

    @Override
    public void put(String original, String translated) {
        instance.getAllCommando2t().put(original, translated);
    }

    @Override
    public @Nullable String getTranslated(String original) {
        return instance.getAllCommando2t().getValue(original);
    }

    @Override
    public boolean containsOriginal(String original) {
        return instance.getAllCommando2t().containsKey(original);
    }

    @Override
    public boolean containsTranslated(String translated) {
        return instance.getAllCommando2t().containsValue(translated);
    }
}
