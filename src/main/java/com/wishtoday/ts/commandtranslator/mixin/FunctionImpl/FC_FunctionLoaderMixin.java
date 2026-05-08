package com.wishtoday.ts.commandtranslator.mixin.FunctionImpl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.wishtoday.ts.commandtranslator.CommandHandler.FunctionTranslationProvider;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.ServiceClass;
import com.wishtoday.ts.commandtranslator.ServiceField;
import com.wishtoday.ts.commandtranslator.Services.Container;
import net.minecraft.resource.Resource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

//TODO: Remove create,processAsync,validateCommandLength,continuesToNextLine,parseAsync. switch to com.wishtoday.ts.commandtranslator.CommandHandler.FunctionTranslationProvider
//Completed
@ServiceClass
@Mixin(FunctionLoader.class)
public abstract class FC_FunctionLoaderMixin {
    @Shadow
    private static List<String> readLines(Resource resource) {
        return null;
    }

    @ServiceField
    private FunctionTranslationProvider translationProvider;

    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> commandDispatcher;

    @Shadow
    private volatile Map<Identifier, CommandFunction<ServerCommandSource>> functions;

    @WrapOperation(method = "method_29449", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<CommandFunction<ServerCommandSource>> wrap(Supplier<CommandFunction<ServerCommandSource>> supplier, Executor executor, Operation<CompletableFuture<CommandFunction<ServerCommandSource>>> original, @Local Map.Entry<Identifier, Resource> entry, @Local ServerCommandSource source, @Local(ordinal = 1) Identifier identifier) {
        List<String> list = readLines(entry.getValue());
        if (list == null) return original.call(supplier, executor);
        CompletableFuture<CommandFunction<ServerCommandSource>> future = CompletableFuture.supplyAsync(
                        () -> translationProvider.create(identifier, commandDispatcher, source, list, executor),
                        executor
                ).thenCompose(f -> f)
                .exceptionallyCompose(t -> {
                    Commandtranslator.LOGGER.error("threw ", t);
                    return original.call(supplier, executor);
                });
        return future;
    }
}
