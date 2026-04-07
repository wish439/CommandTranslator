package com.wishtoday.ts.commandtranslator.Util;

import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextHandleUtils {
    private TextHandleUtils() {}

    @NotNull
    public static String getStringFromText(@NotNull Text text) {
        if (!(text.getContent() instanceof PlainTextContent textContent)) return "";
        return textContent.string();
    }


    //this method returns an Immutable List
    @NotNull
    public static List<String> getStringsFromTexts(@NotNull List<Text> texts) {
        return texts.stream().map(TextHandleUtils::getStringFromText).toList();
    }

    @NotNull
    public static List<Text> getAllSibLingsText(@NotNull Text text, List<Text> originalList) {
        List<Text> siblings = text.getSiblings();
        originalList.addAll(siblings);
        for (Text sibling : siblings) {
            getAllSibLingsText(sibling, originalList);
        }
        return Collections.synchronizedList(originalList.stream().distinct().collect(Collectors.toList()));
    }

    @NotNull
    public static List<Text> handleAllStringInText(@NotNull List<Text> texts, Function<String, String> function) {

        return texts.stream()
                //.filter(text1 -> text1.getContent() instanceof PlainTextContent)
                .map(text1 -> {
                    if (!(text1.getContent() instanceof PlainTextContent content)) {
                        return text1;
                    }
                    String s = content.string();
                    String apply = function.apply(s);

                    MutableText mutableText = MutableText.of(PlainTextContent.of(apply)).setStyle(text1.getStyle());
                    text1.getSiblings().forEach(mutableText::append);
                    return mutableText;
                }).toList();
    }

    @NotNull
    public static CompletableFuture<List<Text>> handleAllStringInTextAsync(
            @NotNull List<Text> texts,
            Function<String, CompletableFuture<String>> function
    ) {
        List<CompletableFuture<Text>> futures = texts.stream().distinct()
                //.filter(text1 -> text1.getContent() instanceof PlainTextContent)
                .map(text1 -> {
                    if (!(text1.getContent() instanceof PlainTextContent content)) {
                        return CompletableFuture.completedFuture(text1);
                    }

                    String s = content.string();

                    return function.apply(s).thenApply(translated -> {
                        MutableText mutableText = MutableText
                                .of(PlainTextContent.of(translated))
                                .setStyle(text1.getStyle());

                        text1.getSiblings().forEach(mutableText::append);
                        return (Text) mutableText;
                    });
                })
                .toList();
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                );
    }

    @NotNull
    public static Text buildNewText(String newPlainText, Style style, List<Text> siblings) {
        Text newText = Text.of(newPlainText);
        MutableText mutableText = ((MutableText) newText).setStyle(style);
        siblings.forEach(mutableText::append);
        return mutableText;
    }
}
