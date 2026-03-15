package com.wishtoday.ts.commandtranslator;

import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Cache.DataSaver;
import com.wishtoday.ts.commandtranslator.Cache.JsonSaver;
import com.wishtoday.ts.commandtranslator.Processor.*;
import com.wishtoday.ts.commandtranslator.Translator.TranslatorFactory;
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Config.ConfigLoader;
import com.wishtoday.ts.commandtranslator.http.ITranslators;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Commandtranslator implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Commandtranslator.class);
    public static DataSaver dataSaver;

    public static final String MOD_ID = "commandtranslator";

    @Getter
    private static BatchTranslatorProcessorWrapper processorWrapper;

    public static final String CONFIG_FILE_NAME = "commandtranslator.toml";
    public static final String DataPackName = "zzzzz___generate";

    public static ITranslators translator;

    @Getter
    @Setter
    private static boolean modActive = true;

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

        processorWrapper = new BatchTranslatorProcessorWrapper(new BatchTranslatorProcessor(config.getBatchSize(), config.getTimeout(), translator));

        dataSaver = new JsonSaver();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> dataSaver.save(CacheInstance.getINSTANCE());

        ScheduledExecutorService service2 = Executors.newScheduledThreadPool(1);
        service2.scheduleWithFixedDelay(() -> processorWrapper.getWrapped().tick(), 10, 10, TimeUnit.MILLISECONDS);

        this.registerProcessor();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            service.scheduleWithFixedDelay(runnable, 30, 60, TimeUnit.SECONDS);
            service2.shutdown();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> runnable.run());
        this.registerEvents();

    }

    public static void reload() {
        Config load;
        try {
            load = ConfigLoader.load(Config::new, FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME));
        } catch (Exception e) {
            return;
        }

        if (!modActive) {
            load = null;
        }
        Config.setConfig(load);
    }

    private void registerProcessor() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ProcessorHandler handler = ((ProcessorHandlerInterface) server).getProcessorHandler();
            handler.registerProcessor(new TranslationTaskProcessor(5, server));
            handler.registerProcessor(getProcessorWrapper().getWrapped());
        });
    }

    private void registerEvents() {
        ServerTickEvents.START_SERVER_TICK.register(server -> ((ProcessorHandlerInterface) server).getProcessorHandler().tick());
    }
}
