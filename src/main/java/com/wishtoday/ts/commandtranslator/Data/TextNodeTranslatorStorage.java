package com.wishtoday.ts.commandtranslator.Data;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.wishtoday.ts.commandtranslator.CommandTranslator.ArgumentTranslator;
import lombok.Getter;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Function;

@Getter
public final class TextNodeTranslatorStorage<T> {

    private final String argumentName;
    private final ArgumentTranslator<T> translator;

    public TextNodeTranslatorStorage(String argumentName, ArgumentTranslator<T> translator) {
        this.argumentName = argumentName;
        this.translator = translator;
    }

    @SuppressWarnings("unchecked")
    public <S extends AbstractServerCommandSource<S>> TranslateResults<T> translate(CommandContextBuilder<S> context, Function<String, String> o2nFunction) {
        ParsedArgument<S, ?> argument = context.getArguments().get(argumentName);
        if (argument == null) return null;
        return this.translator.translate((T) argument.getResult(), argument.getRange(), o2nFunction);
    }
}
