package com.wishtoday.ts.commandtranslator.Data;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.wishtoday.ts.commandtranslator.CommandHandler.CommandTranslator.ArgumentTranslator;
import net.minecraft.server.command.AbstractServerCommandSource;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record TextNodeTranslatorStorage<T>(String argumentName, ArgumentTranslator<T> translator) {

    @SuppressWarnings("unchecked")
    public <S extends AbstractServerCommandSource<S>> TranslateResults<T> translate(CommandContextBuilder<S> context, Function<String, String> o2nFunction) {
        ParsedArgument<S, ?> argument = context.getArguments().get(argumentName);
        if (argument == null) return null;
        return this.translator.translate((T) argument.getResult(), argument.getRange(), o2nFunction);
    }

    @SuppressWarnings("unchecked")
    public <S extends AbstractServerCommandSource<S>> CompletableFuture<TranslateResults<T>> translateAsync(CommandContextBuilder<S> context, Function<String, CompletableFuture<String>> o2nFunction) {
        ParsedArgument<S, ?> argument = context.getArguments().get(argumentName);
        if (argument == null) return null;
        return this.translator.translateAsync((T) argument.getResult(), argument.getRange(), o2nFunction);
    }
}
