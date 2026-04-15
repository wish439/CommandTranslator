package com.wishtoday.ts.commandtranslator.Helper;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.wishtoday.ts.commandtranslator.Cache.CacheCheckResult;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Cache.CacheService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Pair;

public class CacheCommandChecker implements CommandChecker<CacheService, CacheCheckResult> {
    @Override
    public CacheCheckResult check(CommandContextBuilder<ServerCommandSource> context, String command, CacheService type) {
        return type.checkCache(command);
    }
}
