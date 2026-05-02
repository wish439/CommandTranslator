package com.wishtoday.ts.commandtranslator.Processor;

import com.mojang.brigadier.CommandDispatcher;
import com.wishtoday.ts.commandtranslator.CommandHandler.CommandTranslationProvider;
import com.wishtoday.ts.commandtranslator.Services.CreateConstruction;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.*;

//TODO: Remove command parse logic.use switch to com.wishtoday.ts.commandtranslator.CommandHandler
//Completed
public class TranslationTaskProcessor implements Processor<BlockEntity>{

    @CreateConstruction
    public TranslationTaskProcessor(int threads, MinecraftServer server, CommandTranslationProvider provider) {
        this.provider = provider;
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
        this.queue = new LinkedBlockingQueue<>();
    }

    private final int executorSize;
    private final ThreadPoolExecutor executor;
    private final ThreadPoolExecutor translateExecutor;
    private final MinecraftServer server;
    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private final Queue<BlockEntity> queue;
    private final CommandTranslationProvider provider;


    @Override
    public void submitTask(@NotNull BlockEntity blockEntityPos) {
        queue.add(blockEntityPos);
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

        if (originalCommand.startsWith("/")) {
            originalCommand = originalCommand.substring(1);
        }


        provider.translateAsync(originalCommand, dispatcher, commandExecutor.getSource(), TranslateEnvironment.COMMAND_BLOCK);
    }
}