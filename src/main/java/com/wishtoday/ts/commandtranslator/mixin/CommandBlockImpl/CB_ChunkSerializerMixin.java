package com.wishtoday.ts.commandtranslator.mixin.CommandBlockImpl;

import com.llamalad7.mixinextras.sugar.Local;
import com.wishtoday.ts.commandtranslator.Config.CopyToBuilderConfig;
import com.wishtoday.ts.commandtranslator.Processor.ProcessorHandlerInterface;
import com.wishtoday.ts.commandtranslator.Processor.TranslationTaskProcessor;
import com.wishtoday.ts.commandtranslator.Services.Container;
import com.wishtoday.ts.commandtranslator.TranslateEnvironment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ChunkSerializer.class)
public class CB_ChunkSerializerMixin {
    //private static boolean testing = false;
    /**
     * Called when a BlockEntity is loaded from disk during chunk deserialization.
     *
     * <p>Injection point:
     * {@link ChunkSerializer#getEntityLoadingCallback(ServerWorld, NbtCompound)}
     * inside the lambda before {@link WorldChunk#setBlockEntity(BlockEntity)}.
     *
     * <p>This only triggers for BlockEntities loaded from disk, not for
     * player-placed blocks.
     */
    @Inject(method = "method_39797", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V"))
    private static void method(NbtList nbtList, ServerWorld serverWorld, NbtList nbtList2, WorldChunk chunk, CallbackInfo ci, @Local BlockEntity blockEntity) {
        Optional<CopyToBuilderConfig> copyToBuilderConfig = Container.getInstance().get(CopyToBuilderConfig.class);
        if (copyToBuilderConfig.isEmpty()) {
            return;
        }
        CopyToBuilderConfig config = copyToBuilderConfig.get();
        if (!config.canWorkOn(TranslateEnvironment.COMMAND_BLOCK)) return;
        if (config.getCommandTranslateStrategy() != CopyToBuilderConfig.CommandBlockTranslateStrategy.LOADING) return;
        if (blockEntity == null) return;
        if (!(blockEntity instanceof CommandBlockBlockEntity)) return;

        //Commandtranslator.LOGGER.info(blockEntity.getPos().toString());

        MinecraftServer server = serverWorld.getServer();
        ProcessorHandlerInterface handlerInterface = (ProcessorHandlerInterface) server;
        Optional<TranslationTaskProcessor> optional = handlerInterface.getProcessorHandler().getProcessor(TranslationTaskProcessor.class);
        optional.ifPresent(processor -> {
            processor.submitTask(blockEntity);
            //testing = true;
        });
    }
}
