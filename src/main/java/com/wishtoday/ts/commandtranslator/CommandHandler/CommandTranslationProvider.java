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
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.FunctionCreator.FunctionCreatorManager;
import com.wishtoday.ts.commandtranslator.Helper.BooleanCommandChecker;
import com.wishtoday.ts.commandtranslator.Helper.CacheCommandChecker;
import com.wishtoday.ts.commandtranslator.Helper.TextCommandChecker;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Processor.BatchTranslatorProcessor;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import com.wishtoday.ts.commandtranslator.Util.TranslateUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CommandTranslationProvider {
    private final BatchTranslatorProcessor processor;
    public CommandTranslationProvider(BatchTranslatorProcessor processor) {
        this.processor = processor;
    }

    public @NotNull CompletableFuture<String> getTranslationAsync(String string, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, Identifier id, Config.Environment environment) {
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -A");
        if (!Commandtranslator.isModActive()) return CompletableFuture.completedFuture(string);
        //Commandtranslator.LOGGER.info("FC_CommandFunctionMixin.parse called stage -B");
        Config config = Config.getInstance();
        if (!config.canWorkOn(environment)) return CompletableFuture.completedFuture(string);
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
}
