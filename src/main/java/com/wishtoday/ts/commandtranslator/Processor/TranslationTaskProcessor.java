package com.wishtoday.ts.commandtranslator.Processor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.wishtoday.ts.commandtranslator.Cache.CacheInstance;
import com.wishtoday.ts.commandtranslator.Commandtranslator;
import com.wishtoday.ts.commandtranslator.Config.Config;
import com.wishtoday.ts.commandtranslator.Data.TextNodeTranslatorStorage;
import com.wishtoday.ts.commandtranslator.Data.TranslateResults;
import com.wishtoday.ts.commandtranslator.Data.UniqueLinkedBlockingQueue;
import com.wishtoday.ts.commandtranslator.Manager.TextCommandManager;
import com.wishtoday.ts.commandtranslator.Util.CommandParseUtils;
import com.wishtoday.ts.commandtranslator.Util.TranslateUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.UncheckedException;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class TranslationTaskProcessor implements Processor<BlockEntity>{

    public TranslationTaskProcessor(int threads, MinecraftServer server) {
        int max = Math.max(threads, 1);

        this.executorSize = max;

        this.executor = new ThreadPoolExecutor(
                max,
                max,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(max * 4),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.translateExecutor = new ThreadPoolExecutor(
                max,
                max,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(max * 8),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.server = server;
        this.dispatcher = server.getCommandManager().getDispatcher();
        this.queue = new UniqueLinkedBlockingQueue<>();
    }

    private final int executorSize;
    private final ThreadPoolExecutor executor;
    private final ThreadPoolExecutor translateExecutor;
    private final MinecraftServer server;
    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private final UniqueLinkedBlockingQueue<BlockEntity> queue;


    @Override
    public void submitTask(@NotNull BlockEntity blockEntityPos) {
        try {
            queue.put(blockEntityPos);
        } catch (InterruptedException e) {
            Commandtranslator.LOGGER.error("put {} failed {}", blockEntityPos , e );
            throw new UncheckedException(e);
        }
    }

    @Override
    public Class<BlockEntity> getTaskClass() {
        return BlockEntity.class;
    }

    @Override
    public void tick() {
        int limit = Math.min(this.executorSize, queue.size());

        for (int i = 0; i < limit; i++) {

            BlockEntity pos = queue.poll();
            if (pos == null) return;

            executor.execute(() -> process(pos));
        }
    }

    private void process(BlockEntity blockEntity) {
        if (!(blockEntity instanceof CommandBlockBlockEntity commandBlock)) return;

        CommandBlockExecutor commandExecutor = commandBlock.getCommandExecutor();
        String originalCommand = commandExecutor.getCommand();

        CommandNode<ServerCommandSource> head =
                CommandParseUtils.getNodeFromCommandHead(originalCommand, dispatcher);

        if (head == null) return;

        TextCommandManager manager = TextCommandManager.getINSTANCE();
        String name = head.getName();
        if (!"execute".equals(name) && !manager.containsCommand(name)) return;

        ParseResults<ServerCommandSource> parse =
                dispatcher.parse(originalCommand, commandExecutor.getSource());

        CommandContextBuilder<ServerCommandSource> context = parse.getContext();

        context = CommandParseUtils.changeToDeepest(context);

        CommandNode<ServerCommandSource> headNode =
                context.getNodes().getFirst().getNode();

        if (!manager.containsCommand(headNode.getName())) return;
        TextNodeTranslatorStorage<?> storage =
                manager.getCommand(headNode.getName());
        CacheInstance instance = CacheInstance.getINSTANCE();
        if (instance.getAllCommando2t().containsKey(originalCommand)) {
            String value = instance.getAllCommando2t().getValue(originalCommand);
            server.execute(() -> commandExecutor.setCommand(value));
            return;
        }

        Config config = Config.getInstance();

        if (instance.getAllCommando2t().containsValue(originalCommand)) return;

        ProcessorHandler handler = ((ProcessorHandlerInterface) server).getProcessorHandler();

        TranslateResults<?> translated = storage.translate(context, TranslateUtils.getDefaultTranslateStrategy(config, handler.getProcessor(BatchTranslatorProcessor.class).get()));

        if (translated == null) return;

        String s = StringUtils.replaceEach(
                originalCommand,
                translated.original(),
                translated.translated()
        );

        if (originalCommand.equals(s)) return;

        server.execute(() -> commandExecutor.setCommand(s));
        instance.getAllCommando2t().put(originalCommand, s);
    }
}