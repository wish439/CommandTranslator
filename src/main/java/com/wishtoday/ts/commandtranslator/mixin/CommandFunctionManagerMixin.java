package com.wishtoday.ts.commandtranslator.mixin;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.FunctionHandler.FunctionCreator;
import com.wishtoday.ts.commandtranslator.FunctionHandler.FunctionCreatorManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.function.ExpandedMacro;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(CommandFunctionManager.class)
public class CommandFunctionManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "load", at = @At("RETURN"))
    private void loadCommandFunctions(FunctionLoader loader, CallbackInfo ci) {
        Map<Identifier, CommandFunction<ServerCommandSource>> map = loader.getFunctions();
        FunctionCreatorManager instance = FunctionCreatorManager.getInstance();
        Set<Identifier> shouldCoverFunctions = instance.getShouldCoverFunctions();

        List<ExpandedMacro<ServerCommandSource>> collect = map.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof ExpandedMacro<ServerCommandSource>)
                .filter(entry -> shouldCoverFunctions.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(function -> (ExpandedMacro<ServerCommandSource>)function)
                .collect(Collectors.toList());
        FunctionCreator creator = instance.getCreator();
        creator.create(new FunctionCreator.FunctionDataPack(String.format("This is a cover datapack, generate by %s", Commandtranslator.MOD_ID), collect, Commandtranslator.DataPackName), server);
    }
}
