package com.wishtoday.ts.commandtranslator.Cache;

public record CacheCheckResult(boolean success, String translated) {
    public static CacheCheckResult missing() {
        return new CacheCheckResult(false, null);
    }
    public static CacheCheckResult success(String translated) {
        return new CacheCheckResult(true, translated);
    }

    public static CacheCheckResult alreadyExists() {
        return new CacheCheckResult(true, null);
    }
}
