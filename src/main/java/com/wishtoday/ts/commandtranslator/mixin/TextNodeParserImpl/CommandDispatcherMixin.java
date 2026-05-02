package com.wishtoday.ts.commandtranslator.mixin.TextNodeParserImpl;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.ApplicationConfig;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Services.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin {
    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/RootCommandNode;addChild(Lcom/mojang/brigadier/tree/CommandNode;)V"))
    private <S> void register(LiteralArgumentBuilder<S> command
            , CallbackInfoReturnable<LiteralCommandNode<S>> cir
            , @Local final LiteralCommandNode<S> build) {
        if (!Commandtranslator.isModActive()) return;
        Optional<TextCommandManager> textCommandManagerOptional = Container.getInstance().get(TextCommandManager.class);
        if (textCommandManagerOptional.isEmpty()) {
            Commandtranslator.LOGGER.warnWithCaller("Text command manager not found");
        }
        textCommandManagerOptional.ifPresent(textCommandManager -> {
            String literal = build.getLiteral();
            if (!textCommandManager.containsCache(literal)) {
                textCommandManager.clearCache();
                return;
            }
            Optional<ApplicationConfig> configOptional = Container.getInstance().get(ApplicationConfig.class);
            if (configOptional.isEmpty()) {
                return;
            }
            if (!configOptional.get().validateCommand(literal)) {
                textCommandManager.clearCache();
                return;
            }
            TextNodeTranslatorStorage<?> cache = textCommandManager.getCache(literal);
            textCommandManager.addCommand(literal, cache);
            textCommandManager.clearCache();
            System.out.println(literal);
        });

    }
}
