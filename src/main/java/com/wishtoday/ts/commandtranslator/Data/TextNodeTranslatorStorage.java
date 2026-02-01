package com.wishtoday.ts.commandtranslator.Data;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Function;

public record TextNodeTranslatorStorage<T>(String argumentName,
                                           ArgumentType<T> argumentTypes,
                                           Function<T, TranslateResults<T>> translateAction) {
    @SuppressWarnings("unchecked")
    public TranslateResults<T> apply(CommandContextBuilder<ServerCommandSource> context) {
        ParsedArgument<ServerCommandSource, ?> argument = context.getArguments().get(argumentName);
        return this.translateAction.apply((T) argument.getResult());
    }
}
