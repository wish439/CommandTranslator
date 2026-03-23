package com.wishtoday.ts.commandtranslator.mixin.Accessor.CommandFunction;

import net.minecraft.command.SourcedCommandAction;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.ExpandedMacro;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ExpandedMacro.class)
public interface ExpandedMacroAccessor<T> extends CommandFunction<T> {
    @Mutable
    @Final
    @Accessor("entries")
    void setEntries(List<SourcedCommandAction<T>> list);
}
