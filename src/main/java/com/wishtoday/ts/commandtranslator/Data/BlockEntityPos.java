package com.wishtoday.ts.commandtranslator.Data;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
@ToString
public class BlockEntityPos {
    private BlockPos pos;
    private World world;


    public BlockEntityPos(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }

    public BlockEntityPos(BlockEntity blockEntity) {
        this.pos = blockEntity.getPos();
        this.world = blockEntity.getWorld();
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return world.getBlockEntity(pos);
    }
}
