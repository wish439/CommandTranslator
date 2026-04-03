package com.wishtoday.ts.commandtranslator.CommandTranslator;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static com.wishtoday.ts.commandtranslator.Util.TextHandleUtils.*;
import static com.wishtoday.ts.commandtranslator.Util.TextHandleUtils.getStringsFromTexts;

public class TextArgumentTranslator implements ArgumentTranslator<Text> {

    public static final TextArgumentTranslator INSTANCE = new TextArgumentTranslator();

    @Nullable
    @Override
    public TranslateResults<Text> translate(Text text, StringRange range, Function<String, String> o2nFunction) {
        List<String> original = new ArrayList<>();
        List<String> translated = new ArrayList<>();
        if (!(text.getContent() instanceof PlainTextContent content)) return null;
        String plainString = content.string();
        List<Text> texts = getAllSibLingsText(text, Lists.newArrayList());
        String appliedPlain = o2nFunction.apply(plainString);
        List<Text> handled = handleAllStringInText(texts, o2nFunction);

        return getTextTranslateResults(text, range, plainString, texts, appliedPlain, handled, original, translated);
    }

    @Override
    public @NotNull CompletableFuture<TranslateResults<Text>> translateAsync(
            Text text,
            StringRange range,
            Function<String, CompletableFuture<String>> function
    ) {
        if (!(text.getContent() instanceof PlainTextContent content)) {
            Commandtranslator.LOGGER.warn("TextArgumentTranslator.translateAsync: Text is not a PlainTextContent, will return null");
            return CompletableFuture.completedFuture(null);
        }
        String plain = content.string();
        List<Text> siblings = getAllSibLingsText(text, new ArrayList<>());
        CompletableFuture<String> mainFuture = function.apply(plain);
        CompletableFuture<List<Text>> siblingsFuture =
                handleAllStringInTextAsync(siblings, function);
        return mainFuture.thenCombine(siblingsFuture, (mainTranslated, handled) -> {
            List<String> original = new ArrayList<>();
            List<String> translated = new ArrayList<>();
            return getTextTranslateResults(text, range, plain, siblings, mainTranslated, handled, original, translated);
        });
    }

    @Override
    public @NotNull CompletableFuture<TranslateResults<Text>> translateAsyncWithExecutor(
            Text text,
            StringRange range,
            Function<String, CompletableFuture<String>> function,
            Executor executor
    ) {
        if (!(text.getContent() instanceof PlainTextContent content)) {
            Commandtranslator.LOGGER.warn("TextArgumentTranslator.translateAsync: Text is not a PlainTextContent, will return null");
            return CompletableFuture.completedFuture(null);
        }
        String plain = content.string();
        List<Text> siblings = getAllSibLingsText(text, new ArrayList<>());
        CompletableFuture<String> mainFuture = function.apply(plain);
        CompletableFuture<List<Text>> siblingsFuture =
                handleAllStringInTextAsync(siblings, function);
        return mainFuture.thenCombine(siblingsFuture, (mainTranslated, handled) -> {
            List<String> original = new ArrayList<>();
            List<String> translated = new ArrayList<>();
            return getTextTranslateResults(text, range, plain, siblings, mainTranslated, handled, original, translated);
        });
    }

    @NotNull
    private TranslateResults<Text> getTextTranslateResults(Text text, StringRange range, String plain, List<Text> siblings, String mainTranslated, List<Text> handled, List<String> original, List<String> translated) {
        original.add(formatStringToReplaceFormat(plain));
        original.addAll(getStringsFromTexts(siblings).stream()
                .map(this::formatStringToReplaceFormat).toList());

        translated.add(formatStringToReplaceFormat(mainTranslated));
        translated.addAll(getStringsFromTexts(handled).stream()
                .map(this::formatStringToReplaceFormat).toList());

        return new TranslateResults<>(
                buildNewText(mainTranslated, text.getStyle(), handled),
                original,
                translated,
                range
        );
    }

    @NotNull
    private String formatStringToReplaceFormat(@NotNull String s) {
        return String.format("\"%s\"", s.replace("\"", "\\\""));
    }
}
