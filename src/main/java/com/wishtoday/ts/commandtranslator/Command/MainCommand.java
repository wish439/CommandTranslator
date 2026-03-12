package com.wishtoday.ts.commandtranslator.Command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;
public class MainCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("translator")
                        .requires(CommandUtils.getOPPermissionLevel())
                        .then(
                                literal("reload")
                                        .executes(
                                                context -> {
                                                    Commandtranslator.reload();
                                                    return Command.SINGLE_SUCCESS;
                                                }
                                        )
                        )
        );
    }
}
