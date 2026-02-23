package com.wishtoday.ts.commandtranslator.Util;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandParseUtils {
    public static <T> void changeToDeepest(CommandContextBuilder<T> context) {
        while (context.getChild() != null) {
            context = context.getChild();
        }
    }

    public static <S> List<CommandNode<S>> getAllChildrenAndItSelf(CommandNode<S> node) {
        List<CommandNode<S>> result = new ArrayList<>();
        fillFromAllChildren(node, result);
        result.addFirst(node);
        return result;
    }

    public static <S> void fillFromAllChildren(CommandNode<S> node
            , List<CommandNode<S>> result) {
        if (result.contains(node)) return;
        Collection<CommandNode<S>> children = node.getChildren();
        result.addAll(children);
        for (CommandNode<S> child : children) {
            fillFromAllChildren(child, result);
        }
    }
}
