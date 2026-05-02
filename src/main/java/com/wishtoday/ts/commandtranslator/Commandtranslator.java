package com.wishtoday.ts.commandtranslator;

import com.wishtoday.ts.commandtranslator.Cache.*;
import com.wishtoday.ts.commandtranslator.Command.MainCommand;
import com.wishtoday.ts.commandtranslator.CommandHandler.CommandTranslationProvider;
import com.wishtoday.ts.commandtranslator.CommandHandler.FunctionTranslationProvider;
import com.wishtoday.ts.commandtranslator.Config.*;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.DisplayControlAttitudeAdapter;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.RangeAttitudeAdapter;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.TranslatableCommentAttitudeAdapter;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.DisplayControlAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.RangeAttitude;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.FunctionCreator.FunctionCreatorManager;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Processor.*;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.Services.ObjectFactory;
import com.wishtoday.ts.commandtranslator.Translator.TranslatorFactory;
import com.wishtoday.ts.commandtranslator.http.ITranslator;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//Project TODO: Reduce static field, switch com.wishtoday.ts.commandtranslator.Services.Container
//promoting
public class Commandtranslator implements ModInitializer {

    @Getter
    private static final Commandtranslator Instance = new Commandtranslator();

    public static final LoggerExtension LOGGER = new LoggerExtension(LoggerFactory.getLogger(Commandtranslator.class));
    private IConfigLoader<CopyToBuilderConfig> configLoader;

    public static final String MOD_ID = "commandtranslator";

    public static final String CONFIG_FILE_NAME = "commandtranslator.toml";
    public static final String DataPackName = "zzzzz___generate";

    @Getter
    @Setter
    private static boolean modActive = true;

    @Override
    public void onInitialize() {

        //if (loadConfig()) return;

        configLoader = ConfigLoaderBuilder
                .<CopyToBuilderConfig>builderConfigLoader()
                .registerAttitudeAdapter(TranslatableCommentAttitude.class, new TranslatableCommentAttitudeAdapter())
                .registerAttitudeAdapter(RangeAttitude.class, new RangeAttitudeAdapter())
                .registerAttitudeAdapter(DisplayControlAttitude.class, new DisplayControlAttitudeAdapter())
                .buildBuilderConfigLoader();

        this.registerServices();

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Optional<DataSaver> saver = Container.getInstance().get(DataSaver.class);
        DataSaver dataSaver = saver.orElse(null);
        assert dataSaver != null;
        Optional<CacheService> serviceOptional = Container.getInstance().get(CacheService.class);
        final CacheService cacheService = serviceOptional.orElse(null);
        assert cacheService != null;
        Runnable runnable = () -> dataSaver.save(cacheService.getCacheInstance());

        Optional<BatchTranslatorProcessor> processorOptional = Container.getInstance().get(BatchTranslatorProcessor.class);
        BatchTranslatorProcessor processor = processorOptional.orElse(null);
        assert processor != null;

        ScheduledExecutorService service2 = Executors.newScheduledThreadPool(1);
        service2.scheduleWithFixedDelay(processor::tick, 10, 10, TimeUnit.MILLISECONDS);

        this.registerProcessor();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            service.scheduleWithFixedDelay(runnable, 30, 60, TimeUnit.SECONDS);
            service2.shutdown();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> runnable.run());
        this.registerEvents();
        this.registerCommands();
    }

    private void registerServices() {
        Container instance = Container.getInstance();
        CopyToBuilderConfig config = this.reload();

        instance.autoRegister(config);

        ITranslator translator = TranslatorFactory.builder()
                .api(config.getTranslateProvider().getApi())
                .key(config.getTranslateProvider().getKey())
                .model(config.getModel())
                .build()
                .getTranslator(config.getTranslateType());
        instance.autoRegister(translator);

        ObjectFactory factory = new ObjectFactory(instance, config);

        Optional<BatchTranslatorProcessor> batchTranslatorProcessor = factory.create(BatchTranslatorProcessor.class);
        batchTranslatorProcessor.ifPresent(instance::autoRegister);

        instance.autoRegister(new JsonSaver());

        Optional<CacheServiceImpl> cacheService = factory.create(CacheServiceImpl.class);

        cacheService.ifPresent(instance::autoRegister);

        TextCommandManager manager = new TextCommandManager();
        instance.autoRegister(manager);
        FunctionCreatorManager functionCreatorManager = new FunctionCreatorManager();
        instance.autoRegister(functionCreatorManager);

        Optional<CommandTranslationProvider> commandTranslationProvider = factory.create(CommandTranslationProvider.class);
        commandTranslationProvider.ifPresent(instance::autoRegister);

        Optional<FunctionTranslationProvider> functionTranslationProvider = factory.create(FunctionTranslationProvider.class);
        functionTranslationProvider.ifPresent(instance::autoRegister);
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess, environment) -> {
                    MainCommand.register(dispatcher);
                });
    }

    public CopyToBuilderConfig reload() {
        CopyToBuilderConfig load;
        try {
            load = this.configLoader.load(CopyToBuilderConfig::new, FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME));
        } catch (Exception e) {
            return new CopyToBuilderConfig();
        }

        if (!modActive) {
            load = new CopyToBuilderConfig();
        }
        return load;
    }

    private void registerProcessor() {
        Optional<BatchTranslatorProcessor> optional = Container.getInstance().get(BatchTranslatorProcessor.class);
        BatchTranslatorProcessor processor = optional.get();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ProcessorHandler handler = ((ProcessorHandlerInterface) server).getProcessorHandler();
            Optional<CommandTranslationProvider> provider = Container.getInstance().get(CommandTranslationProvider.class);
            provider.ifPresent(
                    a -> handler.registerProcessor(new TranslationTaskProcessor(5, server, a))
            );
            handler.registerProcessor(processor);
        });
    }

    private void registerEvents() {
        ServerTickEvents.START_SERVER_TICK.register(server -> ((ProcessorHandlerInterface) server).getProcessorHandler().tick());
    }
}
