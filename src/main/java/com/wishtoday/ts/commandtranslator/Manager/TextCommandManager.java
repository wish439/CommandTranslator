package com.wishtoday.ts.commandtranslator.Manager;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TextCommandManager {
    @Getter
    private static final TextCommandManager INSTANCE = new TextCommandManager();

    private TextCommandManager() {
        this.textNodeCommands = HashSet.newHashSet(5);
    }
    private final Set<String> textNodeCommands;

    public void addCommand(String command) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("Text commands can't be null or empty");
        }
        this.textNodeCommands.add(command);
    }

    public void forEach(Consumer<String> consumer) {
        this.textNodeCommands.forEach(consumer);
    }

    public boolean containsCommand(String command) {
        return this.textNodeCommands.contains(command);
    }
}
