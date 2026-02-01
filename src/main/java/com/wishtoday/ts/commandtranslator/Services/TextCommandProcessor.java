package com.wishtoday.ts.commandtranslator.Services;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Data.TranslateStringResults;
import com.wishtoday.ts.commandtranslator.Reader.CommandParser;
import com.wishtoday.ts.commandtranslator.Reader.SelectorCommandParser;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.wishtoday.ts.commandtranslator.Util.TextHandleUtils.*;
import static com.wishtoday.ts.commandtranslator.Util.TextHandleUtils.getStringsFromTexts;

public class TextCommandProcessor {
    private CommandContextBuilder<ServerCommandSource> context;
    private CommandDispatcher<ServerCommandSource> dispatcher;
    private Function<String, String> o2nFunction;
    private StringRange range;

    public TextCommandProcessor(CommandContextBuilder<ServerCommandSource> context
            , CommandDispatcher<ServerCommandSource> dispatcher
            , Function<String, String> o2nFunction) {
        this.context = context;
        this.dispatcher = dispatcher;
        this.o2nFunction = o2nFunction;
    }

    public void replaceTranslatedContextNode(String translatedCommand) {
        ParseResults<ServerCommandSource> parse = this.dispatcher.parse(translatedCommand, this.context.getSource());
        CommandNodeFinder.StartState<Text, MessageArgumentType.MessageFormat> node = this.findTextNode(parse.getContext());

        if (!node.hasPresent()) return;



        String nodeName;
        StringRange stringRange;

        Object text;
        if (node.leftPresent()) {
            nodeName = node.getLeft().nodeName();
            stringRange = node.getLeft().range();
            text = node.getLeft().result();
        } else {
            nodeName = node.getRight().nodeName();
            stringRange = node.getRight().range();
            text = node.getRight().result();
        }

        this.changeToDeepest(this.context);

        this.context.withArgument(nodeName, new ParsedArgument<>(stringRange.getStart(), stringRange.getEnd(), text));
    }

    @Nullable
    public TranslateStringResults replaceTheContextNodeAndGetTranslateResult() {
        CommandNodeFinder.StartState<Text, MessageArgumentType.MessageFormat> node = this.findTextNode(this.context);

        if (!node.hasPresent()) return null;

        String nodeName = "";
        if (node.leftPresent()) nodeName = node.getLeft().nodeName();
        else nodeName = node.getRight().nodeName();
        if (nodeName == null || nodeName.isEmpty()) return null;
        Pair<ParsedArgument<ServerCommandSource, ?>, TranslateStringResults> results = this.getParsedArgumentAndTranslateResults(node);
        this.context = this.changeToDeepest(this.context);
        this.context.withArgument(nodeName, results.getLeft());

        return results.getRight();
    }

    private Pair<ParsedArgument<ServerCommandSource, ?>, TranslateStringResults> getParsedArgumentAndTranslateResults(CommandNodeFinder.StartState<Text, MessageArgumentType.MessageFormat> node) {
        List<String> original = new ArrayList<>();
        List<String> translated = new ArrayList<>();
        ParsedArgument<ServerCommandSource, ?> node2;
        if (node.leftPresent()) {
            this.range = node.getLeft().range();
            node2 = this.handleText(node.getLeft().result(), original, translated);
        } else {
            this.range = node.getRight().range();
            node2 = this.handleMessageFormat(node.getRight().result(), original, translated);
        }
        return new Pair<>(node2, new TranslateStringResults(original, translated));
    }

    private CommandNodeFinder
            .StartState<Text, MessageArgumentType.MessageFormat> findTextNode(CommandContextBuilder<ServerCommandSource> context) {
        return CommandNodeFinder
                .withContext(context)
                .either(Text.class, MessageArgumentType.MessageFormat.class)
                .start();
    }

    private ParsedArgument<ServerCommandSource, Text> handleText(Text text
            , List<String> original, List<String> translated) {

        ParsedArgument<ServerCommandSource, Text> node2;
        if (!(text.getContent() instanceof PlainTextContent content)) return null;
        String plainString = content.string();
        List<Text> texts = getAllSibLingsText(text, Lists.newArrayList());
        String appliedPlain = o2nFunction.apply(plainString);
        List<Text> handled = handleAllStringInText(texts, o2nFunction);

        node2 = new ParsedArgument<>(range.getStart(), range.getEnd(), buildNewText(appliedPlain, text.getStyle(), handled));
        original.add(formatStringToReplaceFormat(plainString));
        original.addAll(getStringsFromTexts(texts).stream().map(this::formatStringToReplaceFormat).toList());
        translated.add(formatStringToReplaceFormat(appliedPlain));
        translated.addAll(getStringsFromTexts(handled).stream().map(this::formatStringToReplaceFormat).toList());
        return node2;
    }

    private ParsedArgument<ServerCommandSource, MessageArgumentType.MessageFormat> handleMessageFormat(MessageArgumentType.MessageFormat format
            , List<String> original, List<String> translated) {
        ParsedArgument<ServerCommandSource, MessageArgumentType.MessageFormat> node2;
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
            node2 = new ParsedArgument<>(range.getStart(), range.getEnd(), format1);
        }
        return node2;
    }

    private CommandContextBuilder<ServerCommandSource> changeToDeepest(CommandContextBuilder<ServerCommandSource> context) {
        while (context.getChild() != null) {
            context = context.getChild();
        }
        return context;
    }

    @NotNull
    private String formatStringToReplaceFormat(@NotNull String s) {
        return String.format("\"%s\"", s.replace("\"", "\\\""));
    }
}
