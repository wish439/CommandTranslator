package com.wishtoday.ts.commandtranslator.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Reader.CommandParser;
import com.wishtoday.ts.commandtranslator.Reader.SelectorCommandParser;
import com.wishtoday.ts.commandtranslator.Services.TextProvider;
import com.wishtoday.ts.commandtranslator.Util.CommandFixUtils;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.world.CommandBlockExecutor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/util/function/Supplier;)V"))
    private void execute(ParseResults<ServerCommandSource> parseResults
            , String command, CallbackInfo ci) {
        ServerCommandSource source = parseResults.getContext().getSource();
        if (!(source instanceof ServerCommandSourceAccessor accessor)) return;
        CommandOutput output = accessor.getOutput();
        if (!(output instanceof CommandBlockExecutor)) return;
        TextProvider provider = TextProvider.of(parseResults);
        boolean b = provider.startProvide();
        if (!b) return;
        System.out.println("This command is a Text Command, it execute on command block, Text Parsed:" + provider.getCommandString());
        Text text = provider.getText();
        List<Text> siblings = text.getSiblings();
        for (Text text1 : siblings) {
            System.out.println(text1.getString());
        }
        TextContent content = text.getContent();
        if (content instanceof PlainTextContent plainTextContent) {
            System.out.println("LiteralString:" + plainTextContent.string());
        }
        String nodeName = provider.getNodeName();
        ParsedArgument<ServerCommandSource, ?> node2 = getServerCommandSourceParsedArgument(provider, "HelloWorld");
        parseResults.getContext().withArgument(nodeName, node2);
    }

    @Unique
    private static @NotNull ParsedArgument<ServerCommandSource, ?> getServerCommandSourceParsedArgument(TextProvider provider, String newString) {
        ParsedArgument<ServerCommandSource, ?> node = provider.getTextNode();

        ParsedArgument<ServerCommandSource, ?> node2;
        if (!provider.isMessageFormat()) {
            node2 = new ParsedArgument<>(node.getRange().getStart(), node.getRange().getEnd(), Text.of(newString));
        } else {
            MessageArgumentType.MessageFormat format = provider.getMessageFormatInfo().format();
            String contents = format.contents();
            CommandParser<SelectorCommandParser.ParsedResult> parser = SelectorCommandParser.of(contents);
            SelectorCommandParser.ParsedResult parse = parser.parse();
            //parse.forEach(readElement -> System.out.println(readElement.toString()));
            SelectorCommandParser.ParsedResult result = parse.changeAllText(string -> newString);
            MessageArgumentType.MessageFormat format1 = null;
            String s = result.toString();
            System.out.println(s);
            try {
                format1 = MessageArgumentType.MessageFormat.parse(new StringReader(s), true);
            } catch (CommandSyntaxException e) {
                Commandtranslator.LOGGER.error(e.getMessage());
            }
            if (format1 == null) {
                node2 = node;
            } else {
                node2 = new ParsedArgument<>(node.getRange().getStart(), node.getRange().getEnd(), format1);
            }
            //node2 = new ParsedArgument<>(node.getRange().getStart(), node.getRange().getEnd(), CommandFixUtils.fixMessageFormat(provider.getMessageFormatInfo().format(), newString));
        }
        return node2;
    }
}
