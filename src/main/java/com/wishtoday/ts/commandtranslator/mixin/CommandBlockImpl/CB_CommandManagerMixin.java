package com.wishtoday.ts.commandtranslator.mixin.CommandBlockImpl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheService;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.CopyToBuilderConfig;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import com.wishtoday.ts.commandtranslator.Util.LanguageUtils;
import com.wishtoday.ts.commandtranslator.http.ITranslator;
import com.wishtoday.ts.commandtranslator.mixin.Accessor.ServerCommandSourceAccessor;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

//TODO:fix this class?or remove.
//temporary fix.pass compiler check.
@Mixin(CommandManager.class)
public class CB_CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;


    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/util/function/Supplier;)V"))
    private void execute(ParseResults<ServerCommandSource> parseResults
            , String command, CallbackInfo ci) {
        if (!Commandtranslator.isModActive()) return;
        Optional<CopyToBuilderConfig> optionalCopyToBuilderConfig = Container.getInstance().get(CopyToBuilderConfig.class);
        if (optionalCopyToBuilderConfig.isEmpty()) {
            return;
        }
        CopyToBuilderConfig config = optionalCopyToBuilderConfig.get();
        if (!config.isEnableTranslate() || !config.isTranslateCommandBlocks()) return;
        if (config.getCommandTranslateStrategy() != CopyToBuilderConfig.CommandBlockTranslateStrategy.TRIGGER) return;
        CommandContextBuilder<ServerCommandSource> context = parseResults.getContext();
        ServerCommandSource source = context.getSource();
        if (!(source instanceof ServerCommandSourceAccessor accessor)) return;
        CommandOutput output = accessor.getOutput();
        if (!(output instanceof CommandBlockExecutor executor)) return;

        context = CommandParseUtils.changeToDeepest(context);

        Optional<TextCommandManager> textCommandManagerOptional = Container.getInstance().get(TextCommandManager.class);
        if (textCommandManagerOptional.isEmpty()) {
            return;
        }
        TextCommandManager manager = textCommandManagerOptional.get();
        CommandNode<ServerCommandSource> headNode = context.getNodes().getFirst().getNode();

        if (!manager.containsCommand(headNode.getName())) return;

        TextNodeTranslatorStorage<?> storage = manager.getCommand(headNode.getName());

        Optional<CacheService> serviceOptional = Container.getInstance().get(CacheService.class);
        if (serviceOptional.isEmpty()) {
            return;
        }

        CacheService service = serviceOptional.get();

        String originalCommand = executor.getCommand();

        //TextCommandProcessor processor = new TextCommandProcessor(context, this.dispatcher, s -> "HelloWorld");
        if (service.containsOriginal(originalCommand)) {
            String value = service.getTranslated(originalCommand);

            //processor.replaceTranslatedContextNode(value);

            ParseResults<ServerCommandSource> parse = this.dispatcher.parse(value, context.getSource());
            CommandContextBuilder<ServerCommandSource> parseContext = parse.getContext();
            parseContext = CommandParseUtils.changeToDeepest(parseContext);

            ParsedArgument<ServerCommandSource, ?> argument = parseContext.getArguments().get(storage.argumentName());

            context.withArgument(storage.argumentName(), argument);

            executor.setCommand(value);
            return;
        }

        if (service.containsTranslated(originalCommand)) return;
        //TranslateStringResults right = processor.replaceTheContextNodeAndGetTranslateResult();

/*
        TranslateResults<?> translated = storage.translate(context, o -> {
            if (LanguageUtils.isChineseSentence(o)) {
                return o;
            }
            return "HELLO WORLD!";
        });
*/

        Optional<ITranslator> translatorOptional = Container.getInstance().get(ITranslator.class);
        if (translatorOptional.isEmpty()) {
            return;
        }
        ITranslator translator = translatorOptional.get();
        TranslateResults<?> translated = storage.translate(context, o -> {
            if (LanguageUtils.isChineseSentence(o, config.getChineseSentenceJudgmentRange())) {
                return o;
            }

            CompletableFuture<@NotNull String> future = CompletableFuture.supplyAsync(() -> translator.translation(o));

            return future.join();
        });

        if (translated == null) return;
        StringRange range = translated.getRange();
        context.withArgument(storage.argumentName(), new ParsedArgument<>(range.getStart(), range.getEnd(), translated.getResult()));
        String s = StringUtils.replaceEach(originalCommand, translated.original(), translated.translated());

        if (originalCommand.equals(s)) return;

        executor.setCommand(s);

        service.put(originalCommand, s);
    }
}
