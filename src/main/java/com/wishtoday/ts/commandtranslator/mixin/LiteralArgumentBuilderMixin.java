package com.wishtoday.ts.commandtranslator.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Util.TranslateUtils;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiteralArgumentBuilder.class)
public class LiteralArgumentBuilderMixin {
    @Inject(method = "build()Lcom/mojang/brigadier/tree/LiteralCommandNode;", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/LiteralCommandNode;addChild(Lcom/mojang/brigadier/tree/CommandNode;)V"))
    private <S> void onBuild(CallbackInfoReturnable<LiteralCommandNode<S>> cir, @Local final CommandNode<S> node, @Local final LiteralCommandNode<S> result) {
        TextCommandManager instance = TextCommandManager.getINSTANCE();
        if (!(node instanceof ArgumentCommandNode<?,?> argument)) {
            LiteralCommandNode<?> literal = (LiteralCommandNode<?>) node;
            if (instance.containsCache(literal)) {
                //The latter will cover the former, but this is normal and has no effect.
                TextNodeTranslatorStorage<?> cache = instance.getCache(literal);
                instance.cacheCommand(result, cache);
            }
            return;
        }
        if (argument.getType() instanceof TextArgumentType textNode) {
            instance.cacheCommand(result, new TextNodeTranslatorStorage<>(argument.getName(), textNode, TranslateUtils::translateText));
        }
        if (argument.getType() instanceof MessageArgumentType messageNode) {
            instance.cacheCommand(result, new TextNodeTranslatorStorage<>(argument.getName(), messageNode, TranslateUtils::translateMessageFormat));
        }
    }
}
