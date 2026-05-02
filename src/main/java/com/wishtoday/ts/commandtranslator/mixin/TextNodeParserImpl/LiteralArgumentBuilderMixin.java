package com.wishtoday.ts.commandtranslator.mixin.TextNodeParserImpl;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wishtoday.ts.commandtranslator.Helper.CommandTranslator.MessageFormatArgumentTranslator;
import com.wishtoday.ts.commandtranslator.Helper.CommandTranslator.TextArgumentTranslator;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(LiteralArgumentBuilder.class)
public class LiteralArgumentBuilderMixin {
    @Inject(method = "build()Lcom/mojang/brigadier/tree/LiteralCommandNode;", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/LiteralCommandNode;addChild(Lcom/mojang/brigadier/tree/CommandNode;)V"))
    private <S> void onBuild(CallbackInfoReturnable<LiteralCommandNode<S>> cir
            , @Local final CommandNode<S> node
            , @Local final LiteralCommandNode<S> result) {
        if (!Commandtranslator.isModActive()) return;
        Optional<TextCommandManager> manager = Container.getInstance().get(TextCommandManager.class);
        if (manager.isEmpty()) {
            Commandtranslator.LOGGER.warnWithCaller("Text command manager not found");
        }
        manager.ifPresent(textCommandManager -> {
            List<CommandNode<S>> list = CommandParseUtils.getAllChildrenAndItSelf(node);
            for (CommandNode<S> commandNode : list) {
                if (!(commandNode instanceof ArgumentCommandNode<?,?> argument)) {
                    LiteralCommandNode<?> literal = (LiteralCommandNode<?>) commandNode;
                    if (textCommandManager.containsCache(literal.getLiteral())) {
                        //The latter will cover the former, but this is normal and has no effect.
                        TextNodeTranslatorStorage<?> cache = textCommandManager.getCache(literal.getLiteral());
                        textCommandManager.cacheCommand(result.getLiteral(), cache);
                    }
                    return;
                }
                if (argument.getType() instanceof TextArgumentType textNode) {
                    textCommandManager.cacheCommand(result.getLiteral(), new TextNodeTranslatorStorage<>(argument.getName(), TextArgumentTranslator.INSTANCE));
                }
                if (argument.getType() instanceof MessageArgumentType messageNode) {
                    textCommandManager.cacheCommand(result.getLiteral(), new TextNodeTranslatorStorage<>(argument.getName(), MessageFormatArgumentTranslator.INSTANCE));
                }
            }
        });
    }
}
