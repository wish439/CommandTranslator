package com.wishtoday.ts.commandtranslator.Config;

import java.util.Optional;

public interface ValuableConfig {
    <T> Optional<T> getConfigValue(String key, Class<T> type) throws ReflectiveOperationException;
}
