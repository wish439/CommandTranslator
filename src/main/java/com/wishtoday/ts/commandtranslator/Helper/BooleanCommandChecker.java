package com.wishtoday.ts.commandtranslator.Helper;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.server.command.ServerCommandSource;

public interface BooleanCommandChecker<T> {
    boolean check(CommandContextBuilder<ServerCommandSource> context, String command, T type);

    default boolean check(ParseResults<ServerCommandSource> parse, String command, T type) {
        CommandContextBuilder<ServerCommandSource> context = parse.getContext();
        context = CommandParseUtils.changeToDeepest(context);
        return check(context, command, type);
    }
}
