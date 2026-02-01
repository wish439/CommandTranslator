package com.wishtoday.ts.commandtranslator.Manager;

import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextCommandManager {
    @Getter
    private static final TextCommandManager INSTANCE = new TextCommandManager();

    private TextCommandManager() {
        this.cacheTextNodeCommands = new HashMap<>();
        this.textNodeCommands = new HashMap<>();
    }
    private final HashMap<CommandNode<?>, TextNodeTranslatorStorage<?>> textNodeCommands;
    private final HashMap<CommandNode<?>, TextNodeTranslatorStorage<?>> cacheTextNodeCommands;

    public Set<Map.Entry<CommandNode<?>, TextNodeTranslatorStorage<?>>> cacheForEntry() {
        return this.cacheTextNodeCommands.entrySet();
    }

    public void cacheCommand(CommandNode<?> node, TextNodeTranslatorStorage<?> textNode) {
        this.cacheTextNodeCommands.put(node, textNode);
    }

    public TextNodeTranslatorStorage<?> getCache(CommandNode<?> node) {
        return this.cacheTextNodeCommands.get(node);
    }

    public boolean containsCache(CommandNode<?> node) {
        return this.cacheTextNodeCommands.containsKey(node);
    }

    public void addCommand(CommandNode<?> node, TextNodeTranslatorStorage<?> value) {
        this.textNodeCommands.put(node, value);
    }

    public boolean containsCommand(CommandNode<?> node) {
        return this.textNodeCommands.containsKey(node);
    }

    public TextNodeTranslatorStorage<?> getCommand(CommandNode<?> node) {
        return this.textNodeCommands.get(node);
    }
}
