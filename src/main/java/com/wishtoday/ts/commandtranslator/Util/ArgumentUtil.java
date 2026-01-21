package com.wishtoday.ts.commandtranslator.Util;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.TextArgumentType;

public class ArgumentUtil {
    public static boolean isMessageType(ArgumentType<?> type) {
        return type instanceof TextArgumentType ||
                type instanceof MessageArgumentType;
    }
    public static boolean isMessageType(Object value) {
        return value instanceof Message || value instanceof MessageArgumentType.MessageFormat;
    }
}
