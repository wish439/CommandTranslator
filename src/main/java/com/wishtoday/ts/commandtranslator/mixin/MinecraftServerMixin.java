package com.wishtoday.ts.commandtranslator.mixin;

import com.mojang.datafixers.DataFixer;
import com.wishtoday.ts.commandtranslator.Processor.ProcessorHandlerInterface;
import com.wishtoday.ts.commandtranslator.Processor.ProcessorHandler;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements ProcessorHandlerInterface {
    @Unique
    private ProcessorHandler processorHandler;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        processorHandler = new ProcessorHandler();
    }

    @Override
    public ProcessorHandler getProcessorHandler() {
        return this.processorHandler;
    }
}
