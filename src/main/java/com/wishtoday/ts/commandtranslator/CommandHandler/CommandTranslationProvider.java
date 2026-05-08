package com.wishtoday.ts.commandtranslator.CommandHandler;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheCheckResult;
import com.wishtoday.ts.commandtranslator.Cache.CacheService;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.CopyToBuilderConfig;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.CommandHandler.Helper.BooleanCommandChecker;
import com.wishtoday.ts.commandtranslator.CommandHandler.Helper.CacheCommandChecker;
import com.wishtoday.ts.commandtranslator.CommandHandler.Helper.TextCommandChecker;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;
import com.wishtoday.ts.commandtranslator.Services.CreateConstruction;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CommandTranslationProvider {
    private final BatchTranslatorProcessor processor;
    private final TextCommandManager textCommandManager;
    private final CacheService cacheService;
    private final CopyToBuilderConfig config;
    @CreateConstruction
    public CommandTranslationProvider(BatchTranslatorProcessor processor, TextCommandManager textCommandManager, CacheService cacheService, CopyToBuilderConfig config) {
        this.processor = processor;
        this.textCommandManager = textCommandManager;
        this.cacheService = cacheService;
        this.config = config;
    }

    public @NotNull CompletableFuture<String> translateAsync(String string, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, TranslateEnvironment environment) {
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -A");
        if (!Commandtranslator.isModActive()) return CompletableFuture.completedFuture(string);
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -B");
        if (!this.config.canWorkOn(environment)) return CompletableFuture.completedFuture(string);
        ParseResults<ServerCommandSource> parse = dispatcher.parse(string, source);

        BooleanCommandChecker<TextCommandManager> textCommandChecker = new TextCommandChecker();
        if (!textCommandChecker.check(parse, string, textCommandManager)) return CompletableFuture.completedFuture(string);
        CommandContextBuilder<ServerCommandSource> context = parse.getContext();
        context = CommandParseUtils.changeToDeepest(context);

        CommandNode<ServerCommandSource> headNode = context.getNodes().getFirst().getNode();

        CacheCommandChecker checker = new CacheCommandChecker();

        CacheCheckResult check = checker.check(context, string, cacheService);

        if (check.success()) {
            String cached = check.translated();
            if (cached != null) {
                return CompletableFuture.completedFuture(cached);
            }
            return CompletableFuture.completedFuture(string);
        }

        TextNodeTranslatorStorage<?> storage = textCommandManager.getCommand(headNode.getName());

        if (storage == null) return CompletableFuture.completedFuture(string);
        //Function<String, CompletableFuture<String>> listener = s -> CompletableFuture.completedFuture("HelloWorld");
        //CompletableFuture<? extends TranslateResults<?>> translated = storage.translateAsync(context, listener);
        CompletableFuture<? extends TranslateResults<?>> translated = storage.translateAsync(context, TranslateHelper.getDefaultAsyncTranslateStrategy(config, processor));
        ParsedArgument<ServerCommandSource, ?> argument = context.getArguments().get(storage.argumentName());
        if (argument == null) {
            return CompletableFuture.completedFuture(string);
        }
        StringRange range = argument.getRange();
        if (translated == null) return CompletableFuture.completedFuture(string);

        return translated.thenApply(result -> {
            if (result == null) return string;
            String s = TranslateHelper.getReplacedCommand(string, range, result.getResult());
            if (string.equals(s)) return string;

            cacheService.put(string, s);

            System.out.println("Called AAABBBCCC");
            return s;
        });
    }
}
