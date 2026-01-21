package com.wishtoday.ts.commandtranslator.Services;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.wishtoday.ts.commandtranslator.Util.ArgumentUtil;
import lombok.Getter;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TextCommandFinder {
    private CommandContextBuilder<ServerCommandSource> context;
    @Getter
    private ParsedArgument<ServerCommandSource, ?> textNode;
    @Getter
    private String nodeName;
    private boolean filtered;
    @Getter
    private boolean isMessageFormat;
    @Nullable
    @Getter
    private MessageFormatInfo messageFormatInfo;
    private TextCommandFinder(@NotNull CommandContextBuilder<ServerCommandSource> context) {
        this.context = context;
    }
    private TextCommandFinder(@NotNull ParseResults<ServerCommandSource> parseResults) {
        this.context = parseResults.getContext();
    }

    public static TextCommandFinder of(@NotNull CommandContextBuilder<ServerCommandSource> context) {
        return new TextCommandFinder(context);
    }
    public static TextCommandFinder of(@NotNull ParseResults<ServerCommandSource> parseResults) {
        return new TextCommandFinder(parseResults);
    }
    public boolean hasTextNode() {
        if (this.filtered) return textNode != null;
        changeToChildestCommand();
        Map<String, ParsedArgument<ServerCommandSource, ?>> arguments = context.getArguments();
        for (Map.Entry<String, ParsedArgument<ServerCommandSource, ?>> entry : arguments.entrySet()) {
            ParsedArgument<ServerCommandSource, ?> value = entry.getValue();
            if (value.getResult() instanceof MessageArgumentType.MessageFormat format) {
                this.isMessageFormat = true;
                this.messageFormatInfo = new MessageFormatInfo(format);
            }
            if (ArgumentUtil.isMessageType(value.getResult())) {
                this.nodeName = entry.getKey();
                this.textNode = value;
                this.filtered = true;
                return true;
            }
        }
        this.textNode = null;
        this.nodeName = null;
        this.filtered = true;
        return false;
    }

    private void changeToChildestCommand() {
        CommandContextBuilder<ServerCommandSource> ctx = this.context;
        while (ctx.getChild() != null) {
            ctx = ctx.getChild();
        }
        this.context = ctx;
    }

    public record MessageFormatInfo(MessageArgumentType.MessageFormat format) {

    }
}
