package me.moonscenty.createfurnaceengine.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import me.moonscenty.createfurnaceengine.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PoweredFlywheelBlock extends RotatedPillarKineticBlock implements IBE<PoweredFlywheelBlockEntity> {
    public PoweredFlywheelBlock(Properties properties) { super(properties); }
    @Override public Class<PoweredFlywheelBlockEntity> getBlockEntityClass() { return PoweredFlywheelBlockEntity.class; }
    @Override public BlockEntityType<? extends PoweredFlywheelBlockEntity> getBlockEntityType() { return ModBlockEntityTypes.POWERED_FLYWHEEL.get(); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(AXIS); }
    // Power leaves through one end only. The other end is where the engine's crank reaches in,
    // so there is nothing there to couple to.
    @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        if (face.getAxis() != getRotationAxis(state)) return false;
        BlockState engine = FurnaceEngineBlock.findEngine(state, level, pos);
        return engine == null || face != FurnaceEngineBlock.getCrankSide(engine);
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.LARGE_GEAR.get(state.getValue(AXIS));
    }

    @Override public float getParticleTargetRadius() { return 2f; }
    @Override public float getParticleInitialRadius() { return 1.75f; }

    /** Without this, picking the block yields nothing since it has no item of its own. */
    @Override public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level,
        BlockPos pos, Player player) {
        return AllBlocks.FLYWHEEL.asStack();
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!stillValid(state, level, pos)) level.setBlock(pos, getUnpoweredEquivalent(state), 3);
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return stillValid(state, level, pos); }

    public static boolean stillValid(BlockState state, LevelReader level, BlockPos pos) {
        return FurnaceEngineBlock.isDrivenByEngine(state, level, pos);
    }

    public static BlockState getEquivalent(BlockState state) {
        return ModBlocks.POWERED_FLYWHEEL.get().defaultBlockState()
            .setValue(AXIS, state.getValue(BlockStateProperties.AXIS));
    }

    public static BlockState getUnpoweredEquivalent(BlockState state) {
        return AllBlocks.FLYWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, state.getValue(AXIS));
    }
}
