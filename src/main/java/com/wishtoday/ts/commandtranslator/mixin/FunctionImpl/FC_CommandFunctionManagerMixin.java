package com.wishtoday.ts.commandtranslator.mixin.FunctionImpl;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.ApplicationConfig;
import com.wishtoday.ts.commandtranslator.FunctionCreator.FunctionCreator;
import com.wishtoday.ts.commandtranslator.FunctionCreator.FunctionCreatorManager;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.*;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(CommandFunctionManager.class)
public class FC_CommandFunctionManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "load", at = @At("RETURN"))
    private void loadCommandFunctions(FunctionLoader loader, CallbackInfo ci) {
        if (!Commandtranslator.isModActive()) return;
        Optional<ApplicationConfig> configOptional = Container.getInstance().get(ApplicationConfig.class);
        if (configOptional.isEmpty()) return;
        if (!configOptional.get().canWorkOn(TranslateEnvironment.FUNCTION)) return;
        Optional<FunctionCreatorManager> functionCreatorManager = Container.getInstance().get(FunctionCreatorManager.class);
        functionCreatorManager.ifPresent(functionCreator -> {
            Map<Identifier, CommandFunction<ServerCommandSource>> map = loader.getFunctions();
            Set<Identifier> shouldCoverFunctions = functionCreator.getShouldCoverFunctions();

            List<ExpandedMacro<ServerCommandSource>> collect = map.entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof ExpandedMacro<ServerCommandSource>)
                    .filter(entry -> shouldCoverFunctions.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .map(function -> (ExpandedMacro<ServerCommandSource>)function)
                    .collect(Collectors.toList());
            FunctionCreator creator = functionCreator.getCreator();
            boolean b = creator.create(new FunctionCreator.FunctionDataPack(String.format("This is a cover datapack, generate by %s", Commandtranslator.MOD_ID), collect, Commandtranslator.DataPackName), server);
            //if (b) server.reloadResources(Collections.singleton(Commandtranslator.DataPackName));
            shouldCoverFunctions.clear();
        });
    }
}
