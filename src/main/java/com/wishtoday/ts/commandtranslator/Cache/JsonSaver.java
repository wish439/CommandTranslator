package com.wishtoday.ts.commandtranslator.Cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Util.PathUtil;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonSaver implements DataSaver {

    private Path path;
    private Gson gson;

    public JsonSaver() {
        path = PathUtil.getServerConfigPath().resolve("CommandTranslator").resolve("cache.json");
        if (Files.notExists(path)) {
            this.createCache();
        }
        this.gson = new GsonBuilder()
                .registerTypeAdapter(BiHashMap.class, new BiHashMapAdapter<>())
                .setPrettyPrinting()
                .create();
    }

    private void createCache() {
        if (Files.exists(path)) return;
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            Commandtranslator.LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void save(CacheInstance data) {
        this.createCache();
        try(FileWriter writer = new FileWriter(this.path.toFile())) {
            this.gson.toJson(data, writer);
        } catch (IOException e) {
            Commandtranslator.LOGGER.error(e.getMessage());
        }

        //System.out.println(this.gson.toJson(data));
    }

    @Nullable
    @Override
    public CacheInstance load() {
        this.createCache();
        CacheInstance instance = null;
        try(FileReader reader = new FileReader(this.path.toFile())) {
            instance = this.gson.fromJson(reader, CacheInstance.class);
        } catch (IOException e) {
            Commandtranslator.LOGGER.error(e.getMessage());
            return null;
        }
        if (instance == null || instance.isEmpty()) return null;
        return instance;
    }
}
