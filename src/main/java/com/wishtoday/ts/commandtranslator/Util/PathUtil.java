package com.wishtoday.ts.commandtranslator.Util;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtil {
    @NotNull
    public static Path getServerConfigPath() {
        Path dir = FabricLoader.getInstance().getGameDir();
        Path path = dir.resolve("serverconfig");
        if (!Files.exists(path)) path.toFile().mkdirs();
        return path;
    }
}
