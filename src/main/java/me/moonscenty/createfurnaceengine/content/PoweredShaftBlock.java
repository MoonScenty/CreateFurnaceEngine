package me.moonscenty.createfurnaceengine.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import me.moonscenty.createfurnaceengine.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Legacy output block. Engines only drive Flywheels now, so nothing new becomes a powered shaft;
 * this stays registered so worlds saved before that change still load, and reverts to a plain
 * Shaft once the engine beside it stops claiming it.
 */
public class PoweredShaftBlock extends RotatedPillarKineticBlock implements IBE<PoweredShaftBlockEntity> {
    public PoweredShaftBlock(Properties properties) { super(properties); }

    /** Without this, picking the block yields nothing since it has no item of its own. */
    @Override public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level,
        BlockPos pos, Player player) {
        return AllBlocks.SHAFT.asStack();
    }
    @Override public Class<PoweredShaftBlockEntity> getBlockEntityClass() { return PoweredShaftBlockEntity.class; }
    @Override public BlockEntityType<? extends PoweredShaftBlockEntity> getBlockEntityType() { return ModBlockEntityTypes.POWERED_SHAFT.get(); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(AXIS); }
    @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) { return face.getAxis() == getRotationAxis(state); }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!stillValid(state, level, pos)) level.setBlock(pos, getUnpoweredEquivalent(state), 3);
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return stillValid(state, level, pos); }

    public static boolean stillValid(BlockState state, LevelReader level, BlockPos pos) {
        return FurnaceEngineBlock.isDrivenByEngine(state, level, pos);
    }

    public static BlockState getEquivalent(BlockState state) {
        return ModBlocks.POWERED_SHAFT.get().defaultBlockState().setValue(AXIS, state.getValue(ShaftBlock.AXIS));
    }

    public static BlockState getUnpoweredEquivalent(BlockState state) {
        return AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, state.getValue(AXIS));
    }
}
