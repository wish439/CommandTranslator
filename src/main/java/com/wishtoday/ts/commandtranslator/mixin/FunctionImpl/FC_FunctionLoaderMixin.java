package com.wishtoday.ts.commandtranslator.mixin.FunctionImpl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.FunctionHandler.FunctionCreatorManager;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import com.wishtoday.ts.commandtranslator.Util.TranslateUtils;
import lombok.SneakyThrows;
import net.minecraft.command.SingleCommandAction;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.resource.Resource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.FunctionBuilder;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(FunctionLoader.class)
public abstract class FC_FunctionLoaderMixin {
    @Shadow
    private static List<String> readLines(Resource resource) {
        return null;
    }

    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> commandDispatcher;

    @WrapOperation(method = "method_29449", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<CommandFunction<ServerCommandSource>> wrap(Supplier<CommandFunction<ServerCommandSource>> supplier, Executor executor, Operation<CompletableFuture<CommandFunction<ServerCommandSource>>> original, @Local Map.Entry<Identifier, Resource> entry, @Local ServerCommandSource source, @Local(ordinal = 1) Identifier identifier) {
        List<String> list = readLines(entry.getValue());
        if (list == null) return original.call(supplier, executor);
        try {
            return CompletableFuture.supplyAsync(
                    () -> create(identifier, commandDispatcher, source, list),
                    executor
            ).thenCompose(f -> f);
        } catch (Exception e) {
            return original.call(supplier, executor);
        }
    }

    @Nullable
    private static CompletableFuture<String> processAsync(String string, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, Identifier id) {
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -A");
        if (!Commandtranslator.isModActive()) return null;
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -B");
        Config config = Config.getInstance();
        if (!config.isEnableTranslate() || !config.isTranslateFunctions()) return null;
        ParseResults<ServerCommandSource> parse = dispatcher.parse(string, source);
        CommandContextBuilder<ServerCommandSource> context = parse.getContext();
        context = CommandParseUtils.changeToDeepest(context);

        TextCommandManager manager = TextCommandManager.getINSTANCE();
        CommandNode<ServerCommandSource> headNode = context.getNodes().getFirst().getNode();

        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage A data:{}", headNode);

        if (!manager.containsCommand(headNode.getName())) return null;

        TextNodeTranslatorStorage<?> storage = manager.getCommand(headNode.getName());

        CacheInstance cache = CacheInstance.getINSTANCE();

        if (cache.getAllCommando2t().containsValue(string)) {
            FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage B containsValue block data:{}", id);
            return null;
        }

        String value = cache.getAllCommando2t().getValue(string);
        if (value != null) {
            // Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage C value!=null data:{}:{}", id, value);
            FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
            return null;
        }

        //TranslateStringResults right = processor.replaceTheContextNodeAndGetTranslateResult();

        BatchTranslatorProcessor processor = Commandtranslator.getProcessorWrapper().getWrapped();

        CompletableFuture<? extends TranslateResults<?>> translated = storage.translateAsync(context, TranslateUtils.getDefaultAsyncTranslateStrategy(config, processor));
        if (translated == null) return null;

        return translated.thenApply(result -> {
            String s = StringUtils.replaceEach(string, result.original(), result.translated());
            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage D data:{}", s);

            if (string.equals(s)) return string;

            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage E data:{}, {}, {}", id, s, reader.getString());

            FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
            cache.getAllCommando2t().put(string, s);

            return s;
        });
    }

    @SneakyThrows
    private CompletableFuture<CommandFunction<ServerCommandSource>> create(Identifier id, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, List<String> lines) {
        FunctionBuilder<ServerCommandSource> functionBuilder = (FunctionBuilder<ServerCommandSource>) FunctionBuilder.class.getDeclaredConstructor().newInstance();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

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
                        parsed = parseAsync(dispatcher, source, stringReader, id);
                    } catch (CommandSyntaxException var11) {
                        throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var11.getMessage());
                    }
                }
            }

            if (parsed == null) throw new IllegalArgumentException("CreateAsync Failed");

            CompletableFuture<Void> future = parsed.thenAccept(functionBuilder::addAction);

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(
                v -> functionBuilder.toCommandFunction(id));
    }

    private static void validateCommandLength(CharSequence command) {
        if (command.length() > 2000000) {
            CharSequence charSequence = command.subSequence(0, Math.min(512, 2000000));
            throw new IllegalStateException("Command too long: " + command.length() + " characters, contents: " + charSequence + "...");
        }
    }

    private static boolean continuesToNextLine(CharSequence string) {
        int i = string.length();
        return i > 0 && string.charAt(i - 1) == '\\';
    }

    private static CompletableFuture<SourcedCommandAction<ServerCommandSource>> parseAsync(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, StringReader reader, Identifier id) throws CommandSyntaxException {
        CompletableFuture<String> future = processAsync(reader.getString(), dispatcher, source, id);
        CompletableFuture<SourcedCommandAction<ServerCommandSource>> apply =  future.thenApply(s -> {
            ParseResults<ServerCommandSource> parseResults = dispatcher.parse(s, source);
            try {
                CommandManager.throwException(parseResults);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
            Optional<ContextChain<ServerCommandSource>> optional = ContextChain.tryFlatten(parseResults.getContext().build(reader.getString()));
            if (optional.isEmpty()) {
                try {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return (SourcedCommandAction<ServerCommandSource>) new SingleCommandAction.Sourced<>(reader.getString(), optional.get());
            }
        }).exceptionally(t -> {throw new RuntimeException(t);});
        return apply;
    }
}
