package com.wishtoday.ts.commandtranslator.Data;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.function.Function;

public record TextNodeTranslatorStorage<T>(String argumentName, ArgumentType<T> argumentTypes,
                                           Function<T, TranslateResults<T>> translateAction) {

    /*public TextNodeTranslatorStorage(ArgumentType<T> argumentTypes
            , Function<T, TranslateResults<T>> translateAction, String... arguments) {
        this(String.join(".", arguments), argumentTypes, translateAction);
    }*/

    @SuppressWarnings("unchecked")
    public TranslateResults<T> apply(CommandContextBuilder<ServerCommandSource> context) {
        ParsedArgument<ServerCommandSource, ?> argument = context.getArguments().get(argumentName);
        return this.translateAction.apply((T) argument.getResult());
    }

    @SuppressWarnings("unchecked")
    public T get(CommandContextBuilder<ServerCommandSource> context) {
        Map<String, ParsedArgument<ServerCommandSource, ?>> arguments = context.getArguments();
        ParsedArgument<ServerCommandSource, ?> argument = arguments.get(this.argumentName);
        return (T) argument.getResult();
    }
}
