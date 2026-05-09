package com.wishtoday.ts.commandtranslator.CommandHandler.CommandTranslator;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.wishtoday.ts.commandtranslator.CommandHandler.CommandTranslator.TextHandleHelper.*;

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
        List<Text> handled = handleAllStringInText(texts, o2nFunction);

        return getTextTranslateResults(text, range, plainString, handled);
    }

    @Override
    public @NotNull CompletableFuture<TranslateResults<Text>> translateAsync(
            Text text,
            StringRange range,
            Function<String, CompletableFuture<String>> function
    ) {
        if (!(text.getContent() instanceof PlainTextContent content)) {
            Commandtranslator.LOGGER.warn("TextArgumentTranslator.translateAsync:The Text isn't a PlainTextContent");
            return translateAsyncNotMain(text, range, function);
        }
        return translateAsyncHasMain(text, range, function, content);
        /*String plain = content.string();
        List<Text> siblings = getAllSibLingsText(text, Collections.synchronizedList(new ArrayList<>()));
        CompletableFuture<String> mainFuture = function.apply(plain);
        CompletableFuture<List<Text>> siblingsFuture =
                handleAllStringInTextAsync(siblings, function);
        return mainFuture.thenCombine(siblingsFuture, (mainTranslated, handled) -> {
            List<String> original = new ArrayList<>();
            List<String> translated = new ArrayList<>();
            return getTextTranslateResults(text, range, plain, siblings, mainTranslated, handled, original, translated);
        });*/
    }

    private @NotNull CompletableFuture<TranslateResults<Text>> translateAsyncHasMain(
            Text text,
            StringRange range,
            Function<String, CompletableFuture<String>> function,
            PlainTextContent content
    ) {
        String plain = content.string();
        List<Text> siblings = getAllSibLingsText(text, Collections.synchronizedList(new ArrayList<>()));
        CompletableFuture<String> mainFuture = function.apply(plain)
                .orTimeout(240, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    Commandtranslator.LOGGER.warn("Translation timed out for text: {}", plain);
                    return plain;
                });
        CompletableFuture<List<Text>> siblingsFuture =
                handleAllStringInTextAsync(siblings, function);
        return mainFuture.thenCombine(siblingsFuture, (mainTranslated, handled) -> getTextTranslateResults(text, range, plain, handled));
    }

    private @NotNull CompletableFuture<TranslateResults<Text>> translateAsyncNotMain(
            Text text,
            StringRange range,
            Function<String, CompletableFuture<String>> function
    ) {
        List<Text> siblings = getAllSibLingsText(text, Collections.synchronizedList(new ArrayList<>()));
        CompletableFuture<List<Text>> siblingsFuture =
                handleAllStringInTextAsync(siblings, function);
        return siblingsFuture.thenApply(handled -> getTextTranslateResults(text, range, null, handled));
    }

    @NotNull
    private TranslateResults<Text> getTextTranslateResults(Text text, StringRange range, @Nullable String mainTranslated, List<Text> handled) {
        return new TranslateResults<>(
                buildNewText(mainTranslated, text.getStyle(), handled),
                range
        );
    }
}
