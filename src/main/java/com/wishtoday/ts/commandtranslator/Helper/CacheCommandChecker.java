package com.wishtoday.ts.commandtranslator.Helper;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Pair;

public class CacheCommandChecker implements CommandChecker<CacheInstance, Pair<Boolean, String>> {
    @Override
    public Pair<Boolean, String> check(CommandContextBuilder<ServerCommandSource> context, String command , CacheInstance type) {

        if (type.getAllCommando2t().containsValue(command)) {
            return new Pair<>(true, null);
        }

        String value = type.getAllCommando2t().getValue(command);
        if (value != null) {
            // Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage C value!=null data:{}:{}", id, value);
            return new Pair<>(false, value);
        }
        return new Pair<>(true, null);
    }
}
