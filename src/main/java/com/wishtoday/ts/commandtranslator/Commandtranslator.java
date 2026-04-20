package com.wishtoday.ts.commandtranslator;

import com.wishtoday.ts.commandtranslator.Cache.*;
import com.wishtoday.ts.commandtranslator.Config.ABuilderConfig;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter.CommentAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter.NotDisplayInAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter.RangeAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.AnnotationAdapter.TranslatableCommentAdapter;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Adapter.TypeAdapter.*;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.Comment;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.NotDisplayIn;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.Range;
import com.wishtoday.ts.commandtranslator.Config.AnnotationConfig.Annotation.TranslatableComment;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.TranslatableCommentAttitudeAdapter;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.BuilderConfigLoader;
import com.wishtoday.ts.commandtranslator.Config.ConfigLoaderBuilder;
import com.wishtoday.ts.commandtranslator.Config.IConfigLoader;
import com.wishtoday.ts.commandtranslator.Processor.*;
import com.wishtoday.ts.commandtranslator.Translator.TranslatorFactory;
import com.wishtoday.ts.commandtranslator.Config.Config;
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
    @Getter
    private static CacheService cacheService;
    @Getter
    private static IConfigLoader<Config> configLoader;

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

        configLoader = registerFieldTypeAdapters(
                registerAnnotationAdapters(ConfigLoaderBuilder
                .<Config>annotationConfigLoader())).buildAnnotationConfigLoader();

        IConfigLoader<ABuilderConfig> builderConfigLoader = ConfigLoaderBuilder
                .<ABuilderConfig>builderConfigLoader()
                        .registerAttitudeAdapter(TranslatableCommentAttitude.class, new TranslatableCommentAttitudeAdapter())
                                .buildBuilderConfigLoader();

        builderConfigLoader.load(ABuilderConfig::new, FabricLoader.getInstance().getConfigDir().resolve("testDir.toml"));

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

        cacheService = new CacheServiceImpl(CacheInstance.getINSTANCE());
        this.registerProcessor();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            service.scheduleWithFixedDelay(runnable, 30, 60, TimeUnit.SECONDS);
            service2.shutdown();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> runnable.run());
        this.registerEvents();
    }

    private <T> ConfigLoaderBuilder.AnnotationConfigLoaderStage<T> registerAnnotationAdapters(ConfigLoaderBuilder.AnnotationConfigLoaderStage<T> stage) {
        stage.registerAnnotationAdapter(Comment.class, new CommentAdapter());
        stage.registerAnnotationAdapter(Range.class, new RangeAdapter());
        stage.registerAnnotationAdapter(TranslatableComment.class, new TranslatableCommentAdapter());
        stage.registerAnnotationAdapter(NotDisplayIn.class, new NotDisplayInAdapter());
        return stage;
    }

    private <T> ConfigLoaderBuilder.AnnotationConfigLoaderStage<T> registerFieldTypeAdapters(ConfigLoaderBuilder.AnnotationConfigLoaderStage<T> stage) {
        stage.registerFieldTypeAdapter(new SimpleFieldTypeAdapter());
        stage.registerFieldTypeAdapter(new EnumFieldTypeAdapter());
        stage.registerFieldTypeAdapter(new NullFieldTypeAdapter());
        stage.registerFieldTypeAdapter(new ObjectTypeAdapter());
        return stage;
    }

    public static void reload() {
        Config load;
        try {
            load = configLoader.load(Config::new, FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME));
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
