package com.wishtoday.ts.commandtranslator.Util;

import net.minecraft.command.argument.MessageArgumentType;

public class CommandFixUtils {
    public static MessageArgumentType.MessageFormat fixMessageFormat(
            MessageArgumentType.MessageFormat messageFormat
            , String newContent) {
        MessageArgumentType.MessageSelector[] originalSelectors = messageFormat.selectors();
        //String originalContent = messageFormat.contents();
        //int i = newContent.length() - originalContent.length();
        MessageArgumentType.MessageSelector[] newSelectors = new MessageArgumentType.MessageSelector[originalSelectors.length];
        for (int i1 = 0; i1 < originalSelectors.length; i1++) {
            MessageArgumentType.MessageSelector originalSelector = originalSelectors[i1];
            int originalSelectorSize = originalSelector.end() - originalSelector.start();
            newSelectors[i1] = new MessageArgumentType.MessageSelector(newContent.length(), newContent.length() + originalSelectorSize, originalSelector.selector());
        }
        return new MessageArgumentType.MessageFormat(newContent, newSelectors);
    }
}
