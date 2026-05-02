package com.wishtoday.ts.commandtranslator.CommandHandler.Helper;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.server.command.ServerCommandSource;

public interface CommandChecker<T, R> {
    R check(CommandContextBuilder<ServerCommandSource> context, String command, T type);

    default R check(ParseResults<ServerCommandSource> parse, String command, T type) {
        CommandContextBuilder<ServerCommandSource> context = parse.getContext();
        context = CommandParseUtils.changeToDeepest(context);
        return check(context, command, type);
    }
}
