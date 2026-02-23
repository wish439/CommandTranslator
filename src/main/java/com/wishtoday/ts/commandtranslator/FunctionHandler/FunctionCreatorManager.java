package com.wishtoday.ts.commandtranslator.FunctionHandler;

import lombok.Getter;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class FunctionCreatorManager {
    @Getter
    private static FunctionCreatorManager instance = new FunctionCreatorManager();

    @Getter
    private final FunctionCreator creator;

    @Getter
    private final Set<Identifier> shouldCoverFunctions;

    private FunctionCreatorManager() {
        shouldCoverFunctions = new HashSet<>(10);
        creator = new FunctionCreator();
    }
}
