package com.wishtoday.ts.commandtranslator.mixin;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Reader.CommandParser;
import com.wishtoday.ts.commandtranslator.Reader.SelectorCommandParser;
import com.wishtoday.ts.commandtranslator.Services.CommandNodeFinder;
import com.wishtoday.ts.commandtranslator.Services.TextCommandProcessor;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.world.CommandBlockExecutor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.wishtoday.ts.commandtranslator.Util.TextHandleUtils.*;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/util/function/Supplier;)V"))
    private void execute(ParseResults<ServerCommandSource> parseResults
            , String command, CallbackInfo ci) {
        ServerCommandSource source = parseResults.getContext().getSource();
        if (!(source instanceof ServerCommandSourceAccessor accessor)) return;
        CommandOutput output = accessor.getOutput();
        if (!(output instanceof CommandBlockExecutor executor)) return;
        System.out.println("AAAAA:" + command);
        CacheInstance instance = CacheInstance.getINSTANCE();
        String originalCommand = executor.getCommand();
        TextCommandProcessor processor = new TextCommandProcessor(parseResults.getContext(), this.dispatcher, s -> "HelloWorld");
        if (instance.getAllCommando2t().containsKey(originalCommand)) {
            String value = instance.getAllCommando2t().getValue(originalCommand);
            processor.replaceTranslatedContextNode(value);
            executor.setCommand(value);
            return;
        }
        if (instance.getAllCommando2t().containsValue(originalCommand)) return;
        TranslateResults right = processor.replaceTheContextNodeAndGetTranslateResult();
        if (right == null) return;
        String s = StringUtils.replaceEach(originalCommand, right.original(), right.translated());

        executor.setCommand(s);

        instance.getAllCommando2t().put(originalCommand, s);
    }
}
