package com.wishtoday.ts.test.ContainerTest;

import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.AttitudeAdapter.TranslatableCommentAttitudeAdapter;
import com.wishtoday.ts.commandtranslator.Config.BuilderConfig.Attitude.TranslatableCommentAttitude;
import com.wishtoday.ts.commandtranslator.Config.ConfigLoaderBuilder;
import com.wishtoday.ts.commandtranslator.Config.IConfigLoader;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.Services.ObjectFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

public class MainTest {
    @BeforeAll
    static void beforeAll() {
        Container container = new Container();
        Container.setInstance(container);
    }

    @Test
    public void config() {
        IConfigLoader<Config> builderConfigLoader = ConfigLoaderBuilder
                .<Config>builderConfigLoader()
                .registerAttitudeAdapter(TranslatableCommentAttitude.class, new TranslatableCommentAttitudeAdapter())
                .buildBuilderConfigLoader();
        Config load = builderConfigLoader.load(Config::new, Path.of("F:\\My_Minecraft_Mod\\CommandTranslator\\run\\config\\testDir.toml"));
        Container instance = Container.getInstance();
        ObjectFactory factory = new ObjectFactory(instance, load);
        Optional<DeeplyService> service = factory.create(DeeplyService.class);
        //service.ifPresent(System.out::println);
        if (service.isEmpty()) return;
        service.ifPresent(instance::autoRegister);
    }

    @AfterAll
    public static void testContainer() {
        Container instance = Container.getInstance();
        Optional<DeeplyService> service = instance.get(DeeplyService.class);
        System.out.println("This is testContainer method");
        service.ifPresent(System.out::println);
    }
}
