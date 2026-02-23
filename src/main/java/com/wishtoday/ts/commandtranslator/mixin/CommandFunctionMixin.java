package com.wishtoday.ts.commandtranslator.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.FunctionHandler.FunctionCreatorManager;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandFunction.class)
public interface CommandFunctionMixin {
    @WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/function/CommandFunction;parse(Lcom/mojang/brigadier/CommandDispatcher;Lnet/minecraft/server/command/AbstractServerCommandSource;Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/SourcedCommandAction;"))
    private static <T extends AbstractServerCommandSource<T>> SourcedCommandAction<T> parse(
            CommandDispatcher<T> dispatcher, T source
            , StringReader reader
            , Operation<SourcedCommandAction<T>> original
            , @Local(argsOnly = true) Identifier id) {
        ParseResults<T> parse = dispatcher.parse(reader.getString(), source);
        CommandContextBuilder<T> context = parse.getContext();
        CommandParseUtils.changeToDeepest(context);

        TextCommandManager manager = TextCommandManager.getINSTANCE();
        CommandNode<T> headNode = context.getNodes().getFirst().getNode();

        if (!manager.containsCommand(headNode.getName())) return original.call(dispatcher, source, reader);

        TextNodeTranslatorStorage<?> storage = manager.getCommand(headNode.getName());

        CacheInstance instance = CacheInstance.getINSTANCE();

        String value = instance.getAllCommando2t().getValue(reader.getString());
        if (value != null) {
            return original.call(dispatcher, source, new StringReader(value));
        }

        //TranslateStringResults right = processor.replaceTheContextNodeAndGetTranslateResult();

        if (instance.getAllCommando2t().containsValue(reader.getString())) return original.call(dispatcher, source, reader);

        TranslateResults<?> translated = storage.translate(context, o -> "HELLO WORLD!");
        if (translated == null) return original.call(dispatcher, source, reader);
        String s = StringUtils.replaceEach(reader.getString(), translated.original(), translated.translated());

        FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
        instance.getAllCommando2t().put(reader.getString(), s);

        return original.call(dispatcher, source, new StringReader(s));
    }
}
