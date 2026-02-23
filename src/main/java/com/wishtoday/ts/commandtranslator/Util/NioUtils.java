package com.wishtoday.ts.commandtranslator.Util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class NioUtils {
    public static void createFileAndWrite(Path path, String content) {
        if (!Files.exists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new UncheckedIOException("Failed createDirectories " + path.getParent(), e);
            }
        }

        try {
            Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed createFileAndWrite", e);
        }
    }

    public static void deleteDirectories(@NotNull Path path) {
        File file = path.toFile();
        deleteDirectories(file);
    }

    public static void deleteDirectories(@NotNull File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteDirectories(f);
            }
        } else {
            file.delete();
        }
    }
}
