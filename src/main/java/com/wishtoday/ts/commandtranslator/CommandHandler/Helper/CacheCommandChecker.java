package com.wishtoday.ts.commandtranslator.CommandHandler.Helper;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.wishtoday.ts.commandtranslator.Cache.CacheCheckResult;
import com.wishtoday.ts.commandtranslator.Cache.CacheService;
import net.minecraft.server.command.ServerCommandSource;

public class CacheCommandChecker implements CommandChecker<CacheService, CacheCheckResult> {
    @Override
    public CacheCheckResult check(CommandContextBuilder<ServerCommandSource> context, String command, CacheService type) {
        return type.checkCache(command);
    }
}
