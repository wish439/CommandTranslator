package com.wishtoday.ts.commandtranslator.mixin;

import net.minecraft.command.SourcedCommandAction;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.ExpandedMacro;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Mixin(FunctionLoader.class)
public class FunctionLoaderMixin {

    @Inject(method = "method_29451", at = @At(value = "RETURN"))
    public <T> void method_29451(Map.Entry<Identifier, Resource> entry
            , Identifier identifier
            , ServerCommandSource serverCommandSource
            , CallbackInfoReturnable<CommandFunction<T>> cir) {
        CommandFunction<T> returnValue = cir.getReturnValue();
        Identifier id = returnValue.id();
        if (!(returnValue instanceof ExpandedMacro<?> expandedMacro)) return;
        List<? extends SourcedCommandAction<?>> entries = expandedMacro.entries();
        System.out.println("ID: " + id.toString());
        System.out.println("Actions:");
        for (SourcedCommandAction<?> action : entries) {
            System.out.println(action);
        }
    }
}