package com.wishtoday.ts.commandtranslator.Util;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtil {
    @NotNull
    public static Path getServerConfigPath() {
        return getConfigPath("serverconfig");
    }
    @NotNull
    public static Path getConfigPath(String son) {
        Path dir = FabricLoader.getInstance().getGameDir();
        Path path = dir.resolve(son);
        if (!Files.exists(path)) path.toFile().mkdirs();
        return path;
    }
}
