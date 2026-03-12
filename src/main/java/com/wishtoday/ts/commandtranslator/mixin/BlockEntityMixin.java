package com.wishtoday.ts.commandtranslator.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    @Inject(method = "method_17897", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;read(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V"))
    private static void test(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup, String string, BlockEntity blockEntity, CallbackInfoReturnable<BlockEntity> cir) {
        if (blockEntity == null) return;
        if (!(blockEntity instanceof CommandBlockBlockEntity commandBlock)) return;
        System.out.println("Test method_17897");
        System.out.println("Command:" + commandBlock.getCommandExecutor().getCommand() + "Pos:" + commandBlock.getPos());
    }
}
