package com.wishtoday.ts.commandtranslator.Util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static <T> CommandNode<T> getNodeFromCommandHead(String string, CommandDispatcher<T> rootDispatcher) {
        return getNodeFromCommandHead(new StringReader(string), rootDispatcher);
    }

    @Nullable
    public static <T> CommandNode<T> getNodeFromCommandHead(StringReader reader, CommandDispatcher<T> rootDispatcher) {
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String string = reader.getString();
        String s = string.substring(0, reader.getCursor());
        return rootDispatcher.getRoot().getChild(s);
    }

    public static <T> CommandNode<T> considerExecuteGetNodeFromCommandHead(String string, CommandDispatcher<T> rootDispatcher) {
        if (!string.startsWith("execute")) return getNodeFromCommandHead(string, rootDispatcher);
        StringReader reader = new StringReader(string);
        while (reader.canRead(5) && " run ".equals(string.substring(reader.getCursor(), reader.getCursor() + " run ".length()))) {
            reader.skip();
        }
        reader.setCursor(reader.getCursor() + 5);
        return getNodeFromCommandHead(reader, rootDispatcher);
    }
}
