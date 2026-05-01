package com.wishtoday.ts.commandtranslator.FunctionCreator;

import lombok.Getter;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionCreatorManager {
    @Getter
    private static FunctionCreatorManager instance = new FunctionCreatorManager();

    @Getter
    private final FunctionCreator creator;

    @Getter
    private final Set<Identifier> shouldCoverFunctions;

    private FunctionCreatorManager() {
        shouldCoverFunctions = ConcurrentHashMap.newKeySet(10);
        creator = new FunctionCreator();
    }
}
