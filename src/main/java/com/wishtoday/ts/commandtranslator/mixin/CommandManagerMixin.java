package com.wishtoday.ts.commandtranslator.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Data.TranslateStringResults;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Services.TextCommandProcessor;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.CommandBlockExecutor;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/util/function/Supplier;)V"))
    private void execute(ParseResults<ServerCommandSource> parseResults
            , String command, CallbackInfo ci) {
        CommandContextBuilder<ServerCommandSource> context = parseResults.getContext();
        ServerCommandSource source = context.getSource();
        if (!(source instanceof ServerCommandSourceAccessor accessor)) return;
        CommandOutput output = accessor.getOutput();
        if (!(output instanceof CommandBlockExecutor executor)) return;

        CommandParseUtils.changeToDeepest(context);

        TextCommandManager manager = TextCommandManager.getINSTANCE();
        CommandNode<ServerCommandSource> headNode = context.getNodes().getFirst().getNode();
        if (!manager.containsCommand(headNode.getName())) return;
        TextNodeTranslatorStorage<?> storage = manager.getCommand(headNode.getName());
        Object o = storage.get(context);

        System.out.println(o == null);
        System.out.println(o);
        //TranslateResults<?> apply = storage.apply(context);

        CacheInstance instance = CacheInstance.getINSTANCE();

        String originalCommand = executor.getCommand();

        TextCommandProcessor processor = new TextCommandProcessor(context, this.dispatcher, s -> "HelloWorld");
        if (instance.getAllCommando2t().containsKey(originalCommand)) {
            String value = instance.getAllCommando2t().getValue(originalCommand);
            processor.replaceTranslatedContextNode(value);
            executor.setCommand(value);
            return;
        }
        if (instance.getAllCommando2t().containsValue(originalCommand)) return;
        TranslateStringResults right = processor.replaceTheContextNodeAndGetTranslateResult();
        if (right == null) return;
        String s = StringUtils.replaceEach(originalCommand, right.original(), right.translated());

        executor.setCommand(s);

        instance.getAllCommando2t().put(originalCommand, s);
    }
}
