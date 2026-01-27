package com.wishtoday.ts.commandtranslator.mixin;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Reader.CommandParser;
import com.wishtoday.ts.commandtranslator.Reader.SelectorCommandParser;
import com.wishtoday.ts.commandtranslator.Services.CommandNodeFinder;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.world.CommandBlockExecutor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.wishtoday.ts.commandtranslator.Util.TextHandleUtils.*;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/util/function/Supplier;)V"))
    private void execute(ParseResults<ServerCommandSource> parseResults
            , String command, CallbackInfo ci) {
        ServerCommandSource source = parseResults.getContext().getSource();
        if (!(source instanceof ServerCommandSourceAccessor accessor)) return;
        CommandOutput output = accessor.getOutput();
        if (!(output instanceof CommandBlockExecutor executor)) return;
        CommandNodeFinder.StartState<Text, MessageArgumentType.MessageFormat> start =
                CommandNodeFinder
                        .withContext(parseResults.getContext())
                        .either(Text.class
                                , MessageArgumentType.MessageFormat.class)
                        .start();
        if (!start.hasPresent()) return;
        String nodeName = "";
        if (start.leftPresent()) {
            nodeName = start.getLeft().nodeName();
        }
        if (start.rightPresent()) {
            nodeName = start.getRight().nodeName();
        }
        if (nodeName.isEmpty()) return;
        Pair<ParsedArgument<ServerCommandSource, ?>, TranslateResults> pair = getParsedArgumentAndTranslateResults(start, s -> "HelloWorld");
        if (pair == null) return;
        parseResults.getContext().withArgument(nodeName, pair.getLeft());

        TranslateResults right = pair.getRight();
        String s = StringUtils.replaceEach(executor.getCommand(), right.original(), right.translated());
        executor.setCommand(s);
    }

    @Nullable
    @Unique
    private Pair<ParsedArgument<ServerCommandSource, ?>, TranslateResults> getParsedArgumentAndTranslateResults(CommandNodeFinder.StartState<Text, MessageArgumentType.MessageFormat> provider, Function<String, String> o2nFunction) {
        //ParsedArgument<ServerCommandSource, ?> node = textProvider.getTextNode();

        ParsedArgument<ServerCommandSource, ?> node2;
        List<String> original = new ArrayList<>();
        List<String> translated = new ArrayList<>();
        if (provider.leftPresent()) {
            CommandNodeFinder.ArgumentEntry<Text> node = provider.getLeft();
            Text text = node.result();
            if (!(text.getContent() instanceof PlainTextContent content)) return null;
            String plainString = content.string();
            List<Text> texts = getAllSibLingsText(text, Lists.newArrayList());
            String appliedPlain = o2nFunction.apply(plainString);
            List<Text> handled = handleAllStringInText(texts, o2nFunction);

            node2 = new ParsedArgument<>(node.range().getStart(), node.range().getEnd(), buildNewText(appliedPlain, text.getStyle(), handled));
            original.add(formatStringToReplaceFormat(plainString));
            original.addAll(getStringsFromTexts(texts).stream().map(this::formatStringToReplaceFormat).toList());
            translated.add(formatStringToReplaceFormat(appliedPlain));
            translated.addAll(getStringsFromTexts(handled).stream().map(this::formatStringToReplaceFormat).toList());
        } else {
            CommandNodeFinder.ArgumentEntry<MessageArgumentType.MessageFormat> node = provider.getRight();

            MessageArgumentType.MessageFormat format = node.result();
            String contents = format.contents();
            CommandParser<SelectorCommandParser.ParsedResult> parser = SelectorCommandParser.of(contents);


            SelectorCommandParser.ParsedResult parse = parser.parse();

            parse.getReadElements().stream().filter(e -> !e.isSelector()).map(SelectorCommandParser.ReadElement::content).forEach(original::add);

            SelectorCommandParser.ParsedResult result = parse.changeAllText(o2nFunction);

            result.getReadElements().stream().filter(e -> !e.isSelector()).map(SelectorCommandParser.ReadElement::content).forEach(translated::add);

            MessageArgumentType.MessageFormat format1 = null;
            String s = result.toString();
            try {
                format1 = MessageArgumentType.MessageFormat.parse(new StringReader(s), true);
            } catch (CommandSyntaxException e) {
                Commandtranslator.LOGGER.error(e.getMessage());
            }
            if (format1 == null) {
                return null;
            } else {
                node2 = new ParsedArgument<>(node.range().getStart(), node.range().getEnd(), format1);
            }
            //node2 = new ParsedArgument<>(node.getRange().getStart(), node.getRange().getEnd(), CommandFixUtils.fixMessageFormat(provider.getMessageFormatInfo().format(), newString));
        }
        return new Pair<>(node2, new TranslateResults(original, translated));
    }

    @Unique
    @NotNull
    private String formatStringToReplaceFormat(@NotNull String s) {
        return String.format("\"%s\"", s.replace("\"","\\\""));
    }
}
