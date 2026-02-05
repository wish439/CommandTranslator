package com.wishtoday.ts.commandtranslator.CommandTranslator;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Reader.CommandParser;
import com.wishtoday.ts.commandtranslator.Reader.SelectorCommandParser;
import net.minecraft.command.argument.MessageArgumentType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class MessageFormatArgumentTranslator implements ArgumentTranslator<MessageArgumentType.MessageFormat> {

    public static final MessageFormatArgumentTranslator INSTANCE = new MessageFormatArgumentTranslator();

    @Nullable
    @Override
    public TranslateResults<MessageArgumentType.MessageFormat> translate(MessageArgumentType.MessageFormat format, StringRange range, Function<String, String> o2nFunction) {
        List<String> original = new ArrayList<>();
        List<String> translated = new ArrayList<>();
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
        }
        return new TranslateResults<>(format1, original, translated, range);
    }
}
