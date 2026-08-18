package me.moonscenty.createfurnaceengine.content;

import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PoweredFlywheelBlockEntity extends PoweredBlockEntity {
    public PoweredFlywheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.POWERED_FLYWHEEL.get(), pos, state);
    }

    /** The wheel model is far larger than one block; matches Create's FlywheelBlockEntity. */
    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }
}
