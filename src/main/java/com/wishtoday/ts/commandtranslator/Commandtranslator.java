package com.wishtoday.ts.commandtranslator;

import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Cache.DataSaver;
import com.wishtoday.ts.commandtranslator.Cache.JsonSaver;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Commandtranslator implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Commandtranslator.class);
    public static DataSaver dataSaver;

    public static String MOD_ID = "commandtranslator";

    public static String DataPackName = "zzzzz___generate";

    @Override
    public void onInitialize() {
        dataSaver = new JsonSaver();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> dataSaver.save(CacheInstance.getINSTANCE());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> service.scheduleAtFixedRate(runnable, 30, 60, TimeUnit.SECONDS));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            runnable.run();
        });
    }
}
