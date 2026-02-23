package com.wishtoday.ts.commandtranslator.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandFunction.class)
public class CommandFunctionMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "parse", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;"))
    private static <S> ParseResults<S> parse(
            CommandDispatcher<S> dispatcher
            , StringReader command
            , S source
            , Operation<ParseResults<S>> original
            , @Local(argsOnly = true) LocalRef<StringReader> stringReader) {
        StringReader reader = stringReader.get();
        String s = reader.getString();
        ParseResults<S> parse = original.call(dispatcher, command, source);
        CommandContextBuilder<S> context = parse.getContext();

        CommandParseUtils.changeToDeepest(context);

        TextCommandManager manager = TextCommandManager.getINSTANCE();
        CommandNode<S> headNode = context.getNodes().getFirst().getNode();

        if (!manager.containsCommand(headNode.getName())) return parse;

        TextNodeTranslatorStorage<?> storage = manager.getCommand(headNode.getName());

        CacheInstance instance = CacheInstance.getINSTANCE();

        if (instance.getAllCommando2t().containsValue(s)) return parse;

        TranslateResults<?> translated = storage.translate((CommandContextBuilder<ServerCommandSource>) context, o -> "HELLO WORLD!");
        if (translated == null) return parse;
        StringRange range = translated.getRange();
        context.withArgument(storage.getArgumentName(), new ParsedArgument<>(range.getStart(), range.getEnd(), translated.getResult()));
        String s1 = StringUtils.replaceEach(s, translated.original(), translated.translated());

        stringReader.set(new StringReader(s1));

        return parse;
    }
}
