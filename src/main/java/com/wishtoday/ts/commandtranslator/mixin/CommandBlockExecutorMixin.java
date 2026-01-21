package com.wishtoday.ts.commandtranslator.mixin;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandBlockExecutor.class)
public class CommandBlockExecutorMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    private void executeStart(World world, CallbackInfoReturnable<Boolean> cir) {
        Commandtranslator.CAN_MODIFY.set(true);
    }

    @Inject(method = "execute", at = @At("RETURN"))
    private void executeEnd(World world, CallbackInfoReturnable<Boolean> cir) {
        Commandtranslator.CAN_MODIFY.set(false);
    }
}
