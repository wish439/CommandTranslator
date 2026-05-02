package com.wishtoday.ts.commandtranslator.Processor;

import com.mojang.brigadier.CommandDispatcher;
import com.wishtoday.ts.commandtranslator.CommandHandler.CommandTranslationProvider;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;

//TODO: Remove command parse logic.use switch to com.wishtoday.ts.commandtranslator.CommandHandler
//Completed
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
        this.queue = new LinkedBlockingQueue<>();
    }

    private final int executorSize;
    private final ThreadPoolExecutor executor;
    private final ThreadPoolExecutor translateExecutor;
    private final MinecraftServer server;
    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private final Queue<BlockEntity> queue;


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

        Optional<CommandTranslationProvider> provider = Container.getInstance().get(CommandTranslationProvider.class);

        if (!provider.isPresent()) {
            return;
        }
        CommandTranslationProvider translationProvider = provider.get();

        translationProvider.translateAsync(originalCommand, dispatcher, commandExecutor.getSource(), TranslateEnvironment.COMMAND_BLOCK);
    }
}