package me.moonscenty.createfurnaceengine.content;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredShaftBlockEntity extends GeneratingKineticBlockEntity {
    private BlockPos engineOffset;
    private float generatedSpeed;
    private float generatedCapacity;

    public PoweredShaftBlockEntity(BlockPos pos, BlockState state) { super(me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes.POWERED_SHAFT.get(), pos, state); }

    public void update(BlockPos enginePos, float speed, float capacity) {
        BlockPos offset = worldPosition.subtract(enginePos);
        if (offset.equals(engineOffset) && generatedSpeed == speed && generatedCapacity == capacity) return;
        engineOffset = offset;
        generatedSpeed = speed;
        generatedCapacity = capacity;
        updateGeneratedRotation();
    }

    public void removePower(BlockPos enginePos) {
        if (!isPoweredBy(enginePos)) return;
        engineOffset = null;
        generatedSpeed = generatedCapacity = 0;
        updateGeneratedRotation();
    }

    public boolean canBePoweredBy(BlockPos enginePos) { return engineOffset == null || isPoweredBy(enginePos); }
    private boolean isPoweredBy(BlockPos enginePos) { return worldPosition.subtract(enginePos).equals(engineOffset); }

    @Override
    public float getGeneratedSpeed() {
        return generatedSpeed;
    }

    @Override
    public float calculateAddedStressCapacity() {
        lastCapacityProvided = generatedCapacity;
        return generatedCapacity;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (engineOffset != null) tag.putLong("EngineOffset", engineOffset.asLong());
        tag.putFloat("GeneratedSpeed", generatedSpeed);
        tag.putFloat("GeneratedCapacity", generatedCapacity);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        engineOffset = tag.contains("EngineOffset") ? BlockPos.of(tag.getLong("EngineOffset")) : null;
        generatedSpeed = tag.getFloat("GeneratedSpeed");
        generatedCapacity = tag.getFloat("GeneratedCapacity");
    }
}
