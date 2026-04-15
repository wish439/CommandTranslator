package com.wishtoday.ts.commandtranslator.Helper.Stringer;

import net.minecraft.command.argument.MessageArgumentType;
import org.jetbrains.annotations.NotNull;

public class MessageFormatStringable implements Stringable<MessageArgumentType.MessageFormat> {
    @Override
    public String stringValue(MessageArgumentType.MessageFormat value) {
        return value.contents();
    }

    @Override
    public @NotNull Class<MessageArgumentType.MessageFormat> stringableClass() {
        return MessageArgumentType.MessageFormat.class;
    }
}
