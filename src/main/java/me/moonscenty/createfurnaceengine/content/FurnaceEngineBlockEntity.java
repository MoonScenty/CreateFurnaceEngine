package me.moonscenty.createfurnaceengine.content;

import java.lang.ref.WeakReference;
import java.util.List;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import me.moonscenty.createfurnaceengine.Config;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FurnaceEngineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    // Every speed change makes Create tear the kinetic network down and rebuild it, which drags
    // whatever is attached through zero twice. Each of those transitions adds 5 to that block's
    // flicker tally, and only 1 point decays per tick; at 128 the block is destroyed outright.
    // So the ramp is paced by tick budget, not by RPM: gaps must outlast the 10 points a step
    // costs. Reaching the target in few, larger steps keeps the tally near zero.
    private static final int WARMUP_INTERVAL = 15;
    private static final int WARMUP_STEPS = 12;

    private WeakReference<PoweredBlockEntity> target = new WeakReference<>(null);
    private ScrollOptionBehaviour<RotationDirection> movementDirection;
    private int currentRpm;
    private int warmupTicks;

    public FurnaceEngineBlockEntity(BlockPos pos, BlockState state) { super(me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes.FURNACE_ENGINE.get(), pos, state); }
    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(RotationDirection.class,
            CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this,
            new FurnaceEngineValueBox());
        movementDirection.onlyActiveWhen(() -> {
            PoweredBlockEntity output = getOutput();
            return output == null || !output.hasSource();
        });
        movementDirection.withCallback(value -> {
            if (level != null && !level.isClientSide) updateEngine(false);
        });
        behaviours.add(movementDirection);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) updateEngine(true);
    }

    private void updateEngine(boolean advanceWarmup) {
        FurnaceEngineBlock.connectOutput(level, getBlockState(), worldPosition);
        PoweredBlockEntity output = getOutput();
        if (output == null) return;
        BlockState furnace = level.getBlockState(FurnaceEngineBlock.getFurnacePos(getBlockState(), worldPosition));
        boolean active = furnace.hasProperty(AbstractFurnaceBlock.LIT) && furnace.getValue(AbstractFurnaceBlock.LIT);
        boolean hasHeatSink = hasHeatSink(FurnaceEngineBlock.getFurnacePos(getBlockState(), worldPosition));
        int rpm = hasHeatSink ? Config.HEAT_SINK_RPM.get() : Config.BASE_RPM.get();
        int suPerRpm = hasHeatSink ? Config.HEAT_SINK_SU_PER_RPM.get() : Config.BASE_SU_PER_RPM.get();
        if (!active) {
            currentRpm = 0;
            warmupTicks = 0;
        } else if (currentRpm == 0) {
            currentRpm = 1;
            warmupTicks = 0;
        } else {
            currentRpm = Math.min(currentRpm, rpm);
            if (advanceWarmup && currentRpm < rpm && ++warmupTicks >= WARMUP_INTERVAL) {
                currentRpm = Math.min(rpm, currentRpm + Math.max(1, rpm / WARMUP_STEPS));
                warmupTicks = 0;
            }
        }
        Direction facing = FurnaceEngineBlock.getFacing(getBlockState());
        if (facing.getAxis() == Axis.Y)
            facing = getBlockState().getValue(FurnaceEngineBlock.FACING);
        Axis outputAxis = output.getBlockState().getValue(BlockStateProperties.AXIS);
        int direction = outputAxis == Axis.Y ? 1 : (int) GeneratingKineticBlockEntity.convertToDirection(1, facing);
        if (outputAxis == Axis.Z) direction *= -1;
        if (movementDirection != null && movementDirection.get() == RotationDirection.COUNTER_CLOCKWISE)
            direction *= -1;
        output.update(worldPosition, active ? direction * currentRpm : 0,
            active ? suPerRpm : 0);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("CurrentRpm", currentRpm);
        tag.putInt("WarmupTicks", warmupTicks);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        currentRpm = tag.getInt("CurrentRpm");
        warmupTicks = tag.getInt("WarmupTicks");
    }

    private boolean hasHeatSink(BlockPos furnacePos) {
        ResourceLocation configuredId = ResourceLocation.tryParse(Config.HEAT_SINK_BLOCK.get());
        if (configuredId == null || !BuiltInRegistries.BLOCK.containsKey(configuredId)) return false;
        var heatSink = BuiltInRegistries.BLOCK.get(configuredId);
        for (Direction direction : Direction.values())
            if (level.getBlockState(furnacePos.relative(direction)).is(heatSink)) return true;
        return false;
    }

    public Float getTargetAngle() {
        PoweredBlockEntity output = getOutput();
        if (output == null) return null;

        Direction facing = FurnaceEngineBlock.getFacing(getBlockState());
        Axis facingAxis = facing.getAxis();
        Axis outputAxis = KineticBlockEntityRenderer.getRotationAxisOf(output);
        if (outputAxis == facingAxis) return null;

        float angle = KineticBlockEntityRenderer.getAngleForBe(output, output.getBlockPos(), outputAxis);
        if (outputAxis.isHorizontal()
            && (facingAxis == Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE)) angle *= -1;
        if (outputAxis == Axis.X && facing == Direction.DOWN) angle *= -1;
        return angle;
    }

    public PoweredBlockEntity getOutput() {
        PoweredBlockEntity cached = target.get();
        if (cached != null && !cached.isRemoved() && cached.canBePoweredBy(worldPosition)) return cached;
        BlockEntity blockEntity = level.getBlockEntity(FurnaceEngineBlock.getOutputPos(getBlockState(), worldPosition));
        if (blockEntity instanceof PoweredBlockEntity powered && powered.canBePoweredBy(worldPosition)) {
            target = new WeakReference<>(powered);
            return powered;
        }
        target = new WeakReference<>(null);
        return null;
    }

    @Override
    public void remove() {
        PoweredBlockEntity output = target.get();
        if (output != null) output.removePower(worldPosition);
        super.remove();
    }
}
