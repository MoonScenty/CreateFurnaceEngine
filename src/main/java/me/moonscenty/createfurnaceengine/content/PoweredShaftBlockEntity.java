package me.moonscenty.createfurnaceengine.content;

import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredShaftBlockEntity extends PoweredBlockEntity {
    public PoweredShaftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.POWERED_SHAFT.get(), pos, state);
    }
}
