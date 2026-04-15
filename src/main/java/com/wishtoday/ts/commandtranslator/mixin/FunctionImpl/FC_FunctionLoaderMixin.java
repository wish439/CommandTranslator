package com.wishtoday.ts.commandtranslator.mixin.FunctionImpl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheCheckResult;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Cache.CacheService;
import com.wishtoday.ts.commandtranslator.Cache.CacheServiceImpl;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.FunctionHandler.FunctionCreatorManager;
import com.wishtoday.ts.commandtranslator.Helper.BooleanCommandChecker;
import com.wishtoday.ts.commandtranslator.Helper.CacheCommandChecker;
import com.wishtoday.ts.commandtranslator.Helper.TextCommandChecker;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import com.wishtoday.ts.commandtranslator.Util.TranslateUtils;
import net.minecraft.command.SingleCommandAction;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.resource.Resource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.FunctionBuilder;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
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
        //System.out.println("triggered");

        //try {
          //  return create(identifier, commandDispatcher, source, list, executor);
        //} catch (Exception e) {
            //return original.call(supplier, executor);
        //}
        return CompletableFuture.supplyAsync(
                        () -> create(identifier, commandDispatcher, source, list, executor),
                        executor
                ).thenCompose(f -> f)
                .exceptionallyCompose(t -> {
                    Commandtranslator.LOGGER.error("threw", t);
                    return original.call(supplier, executor);
                });
    }

    private CompletableFuture<CommandFunction<ServerCommandSource>> create(Identifier id, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, List<String> lines, Executor executor) throws IllegalArgumentException {
        FunctionBuilder<ServerCommandSource> functionBuilder = null;
        try {
            functionBuilder = (FunctionBuilder<ServerCommandSource>) FunctionBuilder.class.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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

    private static @NotNull CompletableFuture<String> processAsync(String string, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, Identifier id, Executor executor) {
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -A");
        if (!Commandtranslator.isModActive()) return CompletableFuture.completedFuture(string);
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -B");
        Config config = Config.getInstance();
        if (!config.canWorkOn(Config.Environment.FUNCTION)) return CompletableFuture.completedFuture(string);
        ParseResults<ServerCommandSource> parse = dispatcher.parse(string, source);
        TextCommandManager manager = TextCommandManager.getINSTANCE();
        BooleanCommandChecker<TextCommandManager> textCommandChecker = new TextCommandChecker();
        if (!textCommandChecker.check(parse, string, manager)) return CompletableFuture.completedFuture(string);
        CommandContextBuilder<ServerCommandSource> context = parse.getContext();
        /*CommandContextBuilder<ServerCommandSource> context = parse.getContext();
        context = CommandParseUtils.changeToDeepest(context);

        TextCommandManager manager = TextCommandManager.getINSTANCE();
        CommandNode<ServerCommandSource> headNode = context.getNodes().getFirst().getNode();

        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage A data:{}", headNode);

        if (!manager.containsCommand(headNode.getName())) return null;*/
        context = CommandParseUtils.changeToDeepest(context);

        CommandNode<ServerCommandSource> headNode = context.getNodes().getFirst().getNode();

        /*if (cache.getAllCommando2t().containsValue(string)) {
            FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage B containsValue block data:{}", id);
            return null;
        }

        String value = cache.getAllCommando2t().getValue(string);
        if (value != null) {
            // Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage C value!=null data:{}:{}", id, value);
            FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
            return null;
        }*/

        CacheCommandChecker checker = new CacheCommandChecker();

        CacheService instance = Commandtranslator.getCacheService();

        CacheCheckResult check = checker.check(context, string, instance);

        if (check.success()) {
            FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
            String cached = check.translated();
            if (cached != null) {
                return CompletableFuture.completedFuture(cached);
            }
            return CompletableFuture.completedFuture(string);
        }

        //TranslateStringResults right = processor.replaceTheContextNodeAndGetTranslateResult();

        BatchTranslatorProcessor processor = Commandtranslator.getProcessorWrapper().getWrapped();

        TextNodeTranslatorStorage<?> storage = manager.getCommand(headNode.getName());

        if (storage == null) return CompletableFuture.completedFuture(string);
        //CompletableFuture<? extends TranslateResults<?>> translated = storage.translateAsync(context, TranslateUtils.getDefaultAsyncTranslateStrategy(config, processor));
        Function<String, CompletableFuture<String>> listener = s -> CompletableFuture.completedFuture("HelloWorld");
        //System.out.println("Into listener" + string);
        CompletableFuture<? extends TranslateResults<?>> translated = storage.translateAsync(context, listener);
        ParsedArgument<ServerCommandSource, ?> argument = context.getArguments().get(storage.argumentName());
        if (argument == null) {
            return CompletableFuture.completedFuture(string);
        }
        StringRange range = argument.getRange();
        if (translated == null) return CompletableFuture.completedFuture(string);

        return translated.thenApply(result -> {
            if (result == null) return string;
            //String s = StringUtils.replaceEach(string, result.original(), result.translated());
            String s = TranslateUtils.getReplacedCommand(string, range, result.getResult());
            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage D data:{}", s);

            //System.out.println("processAsync processed:" + s);
            if (string.equals(s)) return string;

            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage E data:{}, {}, {}", id, s, reader.getString());

            FunctionCreatorManager.getInstance().getShouldCoverFunctions().add(id);
            instance.put(string, s);

            return s;
        });
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

    @NotNull
    private static CompletableFuture<SourcedCommandAction<ServerCommandSource>> parseAsync(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, StringReader reader, Identifier id, Executor executor) throws CommandSyntaxException {
        CompletableFuture<String> future = processAsync(reader.getString(), dispatcher, source, id, executor);
        if (future == null) {
            return CompletableFuture.completedFuture(CommandFunction.parse(dispatcher, source, reader));
            //return null;
        }
        CompletableFuture<SourcedCommandAction<ServerCommandSource>> apply = future.thenApply(s -> {
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
            //Optional<ContextChain<ServerCommandSource>> optional = ContextChain.tryFlatten(parseResults.getContext().build(reader.getString()));
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
            //System.out.println("Exception caught while trying to parse the command line: ?:" + "original command:" + reader.getString());
            throw new RuntimeException(t);
        });
        return apply;
    }
}
