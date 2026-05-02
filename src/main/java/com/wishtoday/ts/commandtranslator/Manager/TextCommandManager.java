package com.wishtoday.ts.commandtranslator.Manager;

import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TextCommandManager {

    public TextCommandManager() {
        this.cacheTextNodeCommands = new ConcurrentHashMap<>();
        this.textNodeCommands = new ConcurrentHashMap<>();
    }
    private final Map<String, TextNodeTranslatorStorage<?>> textNodeCommands;
    private final Map<String, TextNodeTranslatorStorage<?>> cacheTextNodeCommands;

    public Set<Map.Entry<String, TextNodeTranslatorStorage<?>>> cacheForEntry() {
        return this.cacheTextNodeCommands.entrySet();
    }

    public void cacheCommand(String node, TextNodeTranslatorStorage<?> textNode) {
        this.cacheTextNodeCommands.put(node, textNode);
    }

    public void deletedCache(String node) {
        this.cacheTextNodeCommands.remove(node);
    }

    public void clearCache() {
        this.cacheTextNodeCommands.clear();
    }

    public TextNodeTranslatorStorage<?> getCache(String node) {
        return this.cacheTextNodeCommands.get(node);
    }

    public boolean containsCache(String node) {
        return this.cacheTextNodeCommands.containsKey(node);
    }

    public void addCommand(String node, TextNodeTranslatorStorage<?> value) {
        this.textNodeCommands.put(node, value);
    }

    public Set<Map.Entry<String, TextNodeTranslatorStorage<?>>> getTextNodeCommands() {
        return this.textNodeCommands.entrySet();
    }

    public boolean containsCommand(String node) {
        return this.textNodeCommands.containsKey(node);
    }

    public TextNodeTranslatorStorage<?> getCommand(String node) {
        return this.textNodeCommands.get(node);
    }
}
