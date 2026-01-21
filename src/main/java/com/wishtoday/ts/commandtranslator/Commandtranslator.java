package com.wishtoday.ts.commandtranslator;

import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commandtranslator implements ModInitializer {

    public static ThreadLocal<Boolean> CAN_MODIFY = ThreadLocal.withInitial(() -> false);
    public static final Logger LOGGER = LoggerFactory.getLogger(Commandtranslator.class);

    @Override
    public void onInitialize() {
        TextCommandManager.getINSTANCE().addCommand("title");
        TextCommandManager.getINSTANCE().addCommand("say");
        TextCommandManager.getINSTANCE().addCommand("me");
        TextCommandManager.getINSTANCE().addCommand("teammsg");
    }
}
