package com.wishtoday.ts.commandtranslator.Command;

import net.minecraft.command.CommandSource;

import java.util.function.Predicate;

public class CommandUtils {
    public static <T extends CommandSource> Predicate<T> getOPPermissionLevel() {
        return source -> source.hasPermissionLevel(2);
    }
}
