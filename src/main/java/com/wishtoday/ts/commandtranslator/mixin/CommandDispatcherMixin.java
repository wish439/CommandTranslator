package com.wishtoday.ts.commandtranslator.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin {
    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/RootCommandNode;addChild(Lcom/mojang/brigadier/tree/CommandNode;)V"))
    private <S> void register(LiteralArgumentBuilder<S> command
            , CallbackInfoReturnable<LiteralCommandNode<S>> cir
            , @Local(name = "build") final LiteralCommandNode<S> build) {
        TextCommandManager instance = TextCommandManager.getINSTANCE();
        String literal = build.getLiteral();
        if (!instance.containsCache(literal)) {
            instance.clearCache();
            return;
        }
        TextNodeTranslatorStorage<?> cache = instance.getCache(literal);
        instance.addCommand(literal, cache);
        instance.clearCache();
    }
}
