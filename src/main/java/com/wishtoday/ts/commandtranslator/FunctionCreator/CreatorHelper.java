package com.wishtoday.ts.commandtranslator.FunctionCreator;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CreatorHelper {
    static void createFileAndWrite(Path path, String content) {
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

    static void createFile(Path path) {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed createDirectories " + path.getParent(), e);
        }
    }

    static void deleteDirectories(@NotNull Path path) {
        if (!path.toFile().exists()) return;
        File file = path.toFile();
        DeleteDir(file);
    }

    static void DeleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    DeleteDir(file);
                }
            }
        }
        dir.delete();
    }
}
