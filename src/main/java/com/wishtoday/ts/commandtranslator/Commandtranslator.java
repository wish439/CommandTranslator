package com.wishtoday.ts.commandtranslator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Cache.DataSaver;
import com.wishtoday.ts.commandtranslator.Cache.JsonSaver;
import com.wishtoday.ts.commandtranslator.Processor.ProcessorHandlerInterface;
import com.wishtoday.ts.commandtranslator.Processor.ProcessorHandler;
import com.wishtoday.ts.commandtranslator.Processor.TranslationTaskProcessor;
import com.wishtoday.ts.commandtranslator.Translator.TranslatorFactory;
import com.wishtoday.ts.commandtranslator.Util.NioUtils;
import com.wishtoday.ts.commandtranslator.config.Config;
import com.wishtoday.ts.commandtranslator.config.ConfigLoader;
import com.wishtoday.ts.commandtranslator.http.ITranslator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Commandtranslator implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Commandtranslator.class);
    public static DataSaver dataSaver;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static final String MOD_ID = "commandtranslator";

    public static final String CONFIG_FILE_NAME = "commandTranslator.json";
    public static final String DataPackName = "zzzzz___generate";

    public static ITranslator translator;

    public static boolean modActive = true;

    @Override
    public void onInitialize() {

        //if (loadConfig()) return;

        reload();

        Config config = Config.getInstance();

        translator = TranslatorFactory.builder()
                .api(config.getProvider().getApi())
                .key(config.getProvider().getKey())
                .model(config.getModel())
                .build().getTranslator(config.getType());

        dataSaver = new JsonSaver();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> dataSaver.save(CacheInstance.getINSTANCE());

        this.registerProcessor();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> service.scheduleAtFixedRate(runnable, 30, 60, TimeUnit.SECONDS));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> runnable.run());
        this.registerEvents();
    }

    public static void reload() {
        Config load;
        try {
            load = ConfigLoader.load(Config::new, FabricLoader.getInstance().getConfigDir().resolve("commandtranslator.toml"));
        } catch (Exception e) {
            return;
        }

        if (!modActive) {
            return;
        }
        Config.setConfig(load);
    }

    private void registerProcessor() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ProcessorHandler handler = ((ProcessorHandlerInterface) server).getProcessorHandler();
            handler.registerProcessor(new TranslationTaskProcessor(5, server));
        });
    }

    private void registerEvents() {
        ServerTickEvents.START_SERVER_TICK.register(server -> ((ProcessorHandlerInterface) server).getProcessorHandler().tick());
    }

    private boolean loadConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        if (!Files.exists(path)) {
            process(path);
            LOGGER.error("{} config created. mod closed", MOD_ID);
            modActive = false;
            return true;
        }

        try (FileReader reader = new FileReader(path.toFile())) {
            Config config = GSON.fromJson(reader, Config.class);
            if (config == null) {
                throw new IOException();
            }

            Config.setConfig(config);
        } catch (IOException | JsonParseException e) {
            LOGGER.error("{} Failed to load config. mod closed {}", MOD_ID, e);
            modActive = false;
            process(path);
            return true;
        }

        if (Config.getInstance().isEmpty()) {
            modActive = false;
            process(path);
            return true;
        }
        return false;
    }

    private void process(Path path) {
        Config.setConfig(null);
        NioUtils.deleteDirectories(path);
        try (FileWriter writer = new FileWriter(path.toFile())) {
            //MAPPER.writeValue(writer, Config.getInstance());
            GSON.toJson(Config.getInstance(), writer);
        } catch (IOException e) {
            return;
        }
    }
}
