package com.wishtoday.ts.commandtranslator.mixin;

import com.wishtoday.ts.commandtranslator.Commandtranslator;
import net.minecraft.text.PlainTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlainTextContent.class)
public interface PlainTextContentMixin {

    @Inject(method = "of", at = @At("HEAD"), cancellable = true)
    private static void of(String string
            , CallbackInfoReturnable<PlainTextContent> cir) {
        if (!Commandtranslator.CAN_MODIFY.get()) return;
        cir.setReturnValue(new PlainTextContent.Literal("Hello1"));
    }
}
