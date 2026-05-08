package com.wishtoday.ts.commandtranslator.CommandHandler;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.FunctionCreator.FunctionCreatorManager;
import com.wishtoday.ts.commandtranslator.Services.CreateConstruction;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import net.minecraft.command.SingleCommandAction;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.FunctionBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//TODO: complete this class.
//complete....?Maybe....
public class FunctionTranslationProvider {

    private final CommandTranslationProvider commandTranslationProvider;
    private final FunctionCreatorManager functionCreatorManager;

    @CreateConstruction
    public FunctionTranslationProvider(CommandTranslationProvider commandTranslationProvider, FunctionCreatorManager functionCreatorManager) {
        this.commandTranslationProvider = commandTranslationProvider;
        this.functionCreatorManager = functionCreatorManager;
    }

    public CompletableFuture<CommandFunction<ServerCommandSource>> create(Identifier id, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, List<String> lines, Executor executor) throws IllegalArgumentException {
        FunctionBuilder<ServerCommandSource> functionBuilder = null;
        try {
            functionBuilder = (FunctionBuilder<ServerCommandSource>) FunctionBuilder.class.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalArgumentException("CreateAsync Failed, Can't create FunctionBuilder");
        }

        List<CompletableFuture<SourcedCommandAction<ServerCommandSource>>> futures = Collections.synchronizedList(new ArrayList<>());

        for (final int[] i = {0}; i[0] < lines.size(); i[0]++) {
            int j = i[0] + 1;
            String string = lines.get(i[0]).trim();
            String string3;
            if (continuesToNextLine(string)) {
                StringBuilder stringBuilder = new StringBuilder(string);
                do {
                    if (++i[0] == lines.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }

                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    String string2 = lines.get(i[0]).trim();
                    stringBuilder.append(string2);
                    validateCommandLength(stringBuilder);
                } while (continuesToNextLine(stringBuilder));

                string3 = stringBuilder.toString();
            } else {
                string3 = string;
            }

            validateCommandLength(string3);
            StringReader stringReader = new StringReader(string3);
            CompletableFuture<SourcedCommandAction<ServerCommandSource>> parsed = null;
            if (stringReader.canRead() && stringReader.peek() != '#') {
                if (stringReader.peek() == '/') {
                    stringReader.skip();
                    if (stringReader.peek() == '/') {
                        throw new IllegalArgumentException("Unknown or invalid command '" + string3 + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
                    }

                    String string2 = stringReader.readUnquotedString();
                    throw new IllegalArgumentException(
                            "Unknown or invalid command '" + string3 + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)"
                    );
                }

                if (stringReader.peek() == '$') {
                    functionBuilder.addMacroCommand(string3.substring(1), j, source);
                } else {
                    try {
                        parsed = parseAsync(dispatcher, source, stringReader, id, executor);
                        /*if (parsed == null) {
                            //System.out.printf("End this method?A:%s, B:%s\n", j, string3);
                            throw new IllegalArgumentException("CreateAsync Failed");
                        }*/
                        //CompletableFuture<Void> future = parsed.thenAcceptAsync(functionBuilder::addAction, executor);

                        futures.add(parsed);
                    } catch (CommandSyntaxException var11) {
                        throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var11.getMessage());
                    }
                }
            }
        }

        FunctionBuilder<ServerCommandSource> finalFunctionBuilder = functionBuilder;
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            for (CompletableFuture<SourcedCommandAction<ServerCommandSource>> f : futures) {
                finalFunctionBuilder.addAction(f.join());
            }
            return finalFunctionBuilder.toCommandFunction(id);
        });
    }

    private void validateCommandLength(CharSequence command) {
        if (command.length() > 2000000) {
            CharSequence charSequence = command.subSequence(0, Math.min(512, 2000000));
            throw new IllegalStateException("Command too long: " + command.length() + " characters, contents: " + charSequence + "...");
        }
    }

    private boolean continuesToNextLine(CharSequence string) {
        int i = string.length();
        return i > 0 && string.charAt(i - 1) == '\\';
    }

    @NotNull
    private CompletableFuture<SourcedCommandAction<ServerCommandSource>> parseAsync(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, StringReader reader, Identifier id, Executor executor) throws CommandSyntaxException {
        CompletableFuture<String> future = commandTranslationProvider.translateAsync(reader.getString(), dispatcher, source, TranslateEnvironment.FUNCTION);
        if (!(future.isDone() &&
                !future.isCancelled() &&
                future.getNow(reader.getString()).equals(reader.getString()))) {
            functionCreatorManager.getShouldCoverFunctions().add(id);
        }
        return future.thenApply(s -> {
            if (s == null) {
                try {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create();
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            ParseResults<ServerCommandSource> parseResults = dispatcher.parse(s, source);
            try {
                CommandManager.throwException(parseResults);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
            //Optional<ContextChain<ServerCommandSource>> optional = ContextChain.tryFlatten(parseResults.getContext().buildAnnotationConfigLoader(reader.getString()));
            Optional<ContextChain<ServerCommandSource>> optional = ContextChain.tryFlatten(parseResults.getContext().build(s));
            if (optional.isEmpty()) {
                try {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return (SourcedCommandAction<ServerCommandSource>) new SingleCommandAction.Sourced<>(s, optional.get());
            }
        }).exceptionally(t -> {
            Commandtranslator.LOGGER.errorWithCaller("Exception caught while trying to parse the command line: ?:" + "original command: {}", reader.getString(), t);
            throw new RuntimeException(t);
        });
    }
}
