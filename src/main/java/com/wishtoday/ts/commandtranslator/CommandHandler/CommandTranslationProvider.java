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
import com.wishtoday.ts.commandtranslator.Config.ApplicationConfig;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.CommandHandler.Helper.BooleanCommandChecker;
import com.wishtoday.ts.commandtranslator.CommandHandler.Helper.CacheCommandChecker;
import com.wishtoday.ts.commandtranslator.CommandHandler.Helper.TextCommandChecker;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.Services.CreateConstruction;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CommandTranslationProvider {
    private final BatchTranslatorProcessor processor;
    private final TextCommandManager textCommandManager;
    private final CacheService cacheService;
    @CreateConstruction
    public CommandTranslationProvider(BatchTranslatorProcessor processor, TextCommandManager textCommandManager, CacheService cacheService) {
        this.processor = processor;
        this.textCommandManager = textCommandManager;
        this.cacheService = cacheService;
    }

    public @NotNull CompletableFuture<String> translateAsync(String string, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, TranslateEnvironment environment) {
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -A");
        if (!Commandtranslator.isModActive()) return CompletableFuture.completedFuture(string);
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -B");
        Optional<ApplicationConfig> configOptional = Container.getInstance().get(ApplicationConfig.class);
        if (configOptional.isEmpty()) return CompletableFuture.completedFuture(string);
        ApplicationConfig config = configOptional.get();
        if (!config.canWorkOn(environment)) return CompletableFuture.completedFuture(string);
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
            String s = TranslateHelper.getReplacedCommand(string, range, result.getResult());
            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage D data:{}", s);

            //System.out.println("processAsync processed:" + s);
            if (string.equals(s)) return string;

            //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage E data:{}, {}, {}", id, s, reader.getString());

            cacheService.put(string, s);

            return s;
        });
    }
}
