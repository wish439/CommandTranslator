package com.wishtoday.ts.commandtranslator.Command;

import net.minecraft.command.CommandSource;

import java.util.function.Predicate;

public class CommandUtils {
    private static final int OPPERMISSIONLEVEL = 2;
    public static <T extends CommandSource> Predicate<T> getOPPermissionLevel() {
        return source -> source.hasPermissionLevel(OPPERMISSIONLEVEL);
    }
}
