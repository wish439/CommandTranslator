package com.wishtoday.ts.commandtranslator.Config;

import java.nio.file.Path;
import java.util.function.Supplier;

public interface IConfigLoader<T> {
    T load(Supplier<T> supplier, Path path);
}
