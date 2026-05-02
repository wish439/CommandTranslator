package com.wishtoday.ts.commandtranslator.CommandHandler.Helper;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TextCommandChecker implements BooleanCommandChecker<TextCommandManager> {
    @Override
    public boolean check(CommandContextBuilder<ServerCommandSource> context, String command, TextCommandManager type) {
        CommandNode<ServerCommandSource> headNode = context.getNodes().getFirst().getNode();

        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage A data:{}", headNode);

        return type.containsCommand(headNode.getName());
    }
}
