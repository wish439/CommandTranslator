package com.wishtoday.ts.commandtranslator.Util;

import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.text.Text;
import org.apache.commons.compress.utils.Lists;

public class TranslateUtils {
    public static TranslateResults<Text> translateText(Text text) {
        return new TranslateResults<>(Text.of("HelloWorld"), Lists.newArrayList(), Lists.newArrayList(), null);
    }
    public static TranslateResults<MessageArgumentType.MessageFormat> translateMessageFormat(MessageArgumentType.MessageFormat format) {
        return new TranslateResults<>(new MessageArgumentType.MessageFormat("HelloWorld", new MessageArgumentType.MessageSelector[0]), Lists.newArrayList(), Lists.newArrayList(), null);
    }
}
