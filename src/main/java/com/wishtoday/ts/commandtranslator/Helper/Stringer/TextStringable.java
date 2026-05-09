package com.wishtoday.ts.commandtranslator.Helper.Stringer;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TextStringable implements Stringable<MutableText> {

    @Override
    public String stringValue(MutableText value) {
        return Text.Serialization.toJsonString(value, DynamicRegistryManager.EMPTY);
    }

    @Override
    public @NotNull Class<MutableText> stringableClass() {
        return MutableText.class;
    }
}
