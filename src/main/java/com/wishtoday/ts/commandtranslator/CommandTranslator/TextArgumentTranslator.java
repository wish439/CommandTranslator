package com.wishtoday.ts.commandtranslator.CommandTranslator;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Util.TranslateUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

        original.add(formatStringToReplaceFormat(plainString));
        original.addAll(getStringsFromTexts(texts).stream().map(this::formatStringToReplaceFormat).toList());
        translated.add(formatStringToReplaceFormat(appliedPlain));
        translated.addAll(getStringsFromTexts(handled).stream().map(this::formatStringToReplaceFormat).toList());
        return new TranslateResults<>(buildNewText(appliedPlain, text.getStyle(), handled), original, translated, range);
    }

    @NotNull
    private String formatStringToReplaceFormat(@NotNull String s) {
        return String.format("\"%s\"", s.replace("\"", "\\\""));
    }
}
