package com.wishtoday.ts.commandtranslator.Services;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import lombok.Getter;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TextProvider {
    private TextCommandFinder finder;
    private CommandContextBuilder<ServerCommandSource> context;
    @Getter
    private String commandString;
    @Getter
    private Message commandMessageObj;
    private TextProvider(@NotNull CommandContextBuilder<ServerCommandSource> context) {
        this.context = context;
        this.finder = TextCommandFinder.of(context);
    }
    private TextProvider(@NotNull ParseResults<ServerCommandSource> results) {
        this.context = results.getContext();
        this.finder = TextCommandFinder.of(results);
    }

    public static TextProvider of(@NotNull CommandContextBuilder<ServerCommandSource> context) {
        return new TextProvider(context);
    }
    public static TextProvider of(@NotNull ParseResults<ServerCommandSource> results) {
        return new TextProvider(results);
    }

    public ParsedArgument<ServerCommandSource, ?> getTextNode() {
        return this.finder.getTextNode();
    }

    public String getNodeName() {
        return this.finder.getNodeName();
    }

    public boolean isMessageFormat() {
        return this.finder.isMessageFormat();
    }

    public TextCommandFinder.MessageFormatInfo getMessageFormatInfo() {
        return this.finder.getMessageFormatInfo();
    }

    public boolean startProvide() {
        if (!this.finder.hasTextNode()) return false;
        ParsedArgument<ServerCommandSource, ?> node = finder.getTextNode();
        Object result = node.getResult();
        if (result instanceof Message message) {
            this.commandMessageObj = message;
            this.commandString = message.getString();
        }
        if (result instanceof MessageArgumentType.MessageFormat format) {
            try {
                this.commandMessageObj = format.format(context.getSource(), false);
                this.commandString = this.commandMessageObj.getString();
            } catch (CommandSyntaxException e) {
                Commandtranslator.LOGGER.error("MessageFormat format failed{}", e.getMessage());
            }
        }
        return this.isPresent();
    }

    @NotNull
    public Text getText() {
        if (!(this.commandMessageObj instanceof Text text)) return Text.empty();
        return text;
    }

    public boolean isPresent() {
        return commandString != null || commandMessageObj != null;
    }

}
