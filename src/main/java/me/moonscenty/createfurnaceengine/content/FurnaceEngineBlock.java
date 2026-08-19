package me.moonscenty.createfurnaceengine.content;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.BlockHelper;
import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import me.moonscenty.createfurnaceengine.registry.ModBlocks;
import net.createmod.catnip.math.VoxelShaper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

public class FurnaceEngineBlock extends FaceAttachedHorizontalDirectionalBlock
    implements SimpleWaterloggedBlock, IWrenchable, IBE<FurnaceEngineBlockEntity> {
    private static final int PLACEMENT_HELPER_ID = PlacementHelpers.register(new PlacementHelper());
    public static final MapCodec<FurnaceEngineBlock> CODEC = simpleCodec(FurnaceEngineBlock::new);

    public FurnaceEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACE, AttachFace.FLOOR)
            .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACE, FACING, WATERLOGGED));
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!isHorizontallyAttached(state)) return false;
        BlockPos furnacePos = getFurnacePos(state, pos);
        BlockState furnace = level.getBlockState(furnacePos);
        return furnace.getBlock() instanceof AbstractFurnaceBlock
            && !isFurnaceFront(furnace, furnacePos, pos)
            && !hasOtherEngine(level, furnacePos, pos);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || !isHorizontallyAttached(state)) return null;
        BlockPos furnacePos = getFurnacePos(state, context.getClickedPos());
        BlockState furnace = context.getLevel().getBlockState(furnacePos);
        if (!(furnace.getBlock() instanceof AbstractFurnaceBlock)
            || isFurnaceFront(furnace, furnacePos, context.getClickedPos())) return null;
        return hasOtherEngine(context.getLevel(), furnacePos, context.getClickedPos()) ? null : state;
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
        BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        IPlacementHelper helper = PlacementHelpers.get(PLACEMENT_HELPER_ID);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hit)
                .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hit);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
        LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return state;
    }

    @Override public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        connectOutput(level, state, pos);
    }

    // Swaps a plain Create Shaft or Flywheel in front of the engine for its powered counterpart.
    static void connectOutput(Level level, BlockState state, BlockPos pos) {
        BlockPos outputPos = getOutputPos(state, pos);
        BlockState output = level.getBlockState(outputPos);
        if (!isOutputValid(state, output)) return;
        if (AllBlocks.FLYWHEEL.has(output))
            level.setBlock(outputPos, PoweredFlywheelBlock.getEquivalent(output), 3);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            BlockPos outputPos = getOutputPos(state, pos);
            BlockState output = level.getBlockState(outputPos);
            if (output.is(ModBlocks.POWERED_SHAFT.get()))
                level.setBlock(outputPos, PoweredShaftBlock.getUnpoweredEquivalent(output), 3);
            else if (output.is(ModBlocks.POWERED_FLYWHEEL.get()))
                level.setBlock(outputPos, PoweredFlywheelBlock.getUnpoweredEquivalent(output), 3);
        }
        IBE.onRemove(state, level, pos, replacement);
    }

    // Traced from block/furnace_engine/head.json. That model is authored upright for the
    // FLOOR/NORTH variant, so these boxes carry the x:90 the blockstate applies to lay it on a
    // wall facing north: (x, y, z) becomes (x, z, 16 - y).
    //
    // The reference is deliberately a horizontal facing rather than the upright model. Laying an
    // upright shape down leaves four equally valid rolls to land on, and picking the wrong one
    // mirrors the outline. Between horizontal facings there is only a rotation about Y, so no
    // such choice exists.
    //
    // The model's particle skirt is left out on purpose: it hangs below the block and is there to
    // colour break particles, not to be collided with.
    private static final VoxelShaper SHAPE = VoxelShaper.forHorizontal(Shapes.or(
        Block.box(1, 1, 13, 15, 15, 16),  // mounting plate, flat against the furnace
        Block.box(6, 3, 1, 12, 13, 13),   // body, reaching out towards the flywheel
        Block.box(3, 4, 3, 6, 12, 11)),   // side plate
        Direction.NORTH);

    @Override public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
        BlockPos pos, CollisionContext context) {
        // FLOOR and CEILING states never survive placement, but they still exist in the state
        // definition and the registry bakes a shape for every one of them on freeze. Their
        // connected direction is UP or DOWN, which the horizontal shaper has no entry for, so
        // fall back to the facing itself rather than handing back a null shape.
        Direction facing = getFacing(state);
        return SHAPE.get(facing.getAxis().isHorizontal() ? facing : state.getValue(FACING));
    }

    // Only the furnace's four side faces are valid. FLOOR would sit the engine on top of the
    // furnace and CEILING would hang it underneath, both of which put the crank on an axis the
    // linkage was never modelled for.
    public static boolean isHorizontallyAttached(BlockState state) {
        return state.getValue(FACE) == AttachFace.WALL;
    }

    public static Direction getFacing(BlockState state) { return getConnectedDirection(state); }
    public static BlockPos getFurnacePos(BlockState state, BlockPos pos) { return pos.relative(getFacing(state).getOpposite()); }
    public static BlockPos getOutputPos(BlockState state, BlockPos pos) { return pos.relative(getFacing(state), 2); }

    public static boolean isFlywheel(BlockState state) {
        return AllBlocks.FLYWHEEL.has(state) || state.is(ModBlocks.POWERED_FLYWHEEL.get());
    }

    // A Flywheel is the only accepted output. Its axis may neither line up with the direction the
    // engine extends in, nor stand upright: either puts the wheel in the plane the crank swings
    // through, and the two end up inside each other.
    public static boolean isOutputValid(BlockState engine, BlockState output) {
        if (!isFlywheel(output)) return false;
        Direction.Axis outputAxis = output.getValue(BlockStateProperties.AXIS);
        return outputAxis != getFacing(engine).getAxis() && outputAxis != Direction.Axis.Y;
    }

    // Shared by both powered blocks to decide whether they should stay powered.
    public static boolean isDrivenByEngine(BlockState output, LevelReader level, BlockPos pos) {
        return findEngine(output, level, pos) != null;
    }

    // The engine driving this output block, or null when nothing valid reaches it.
    public static BlockState findEngine(BlockState output, LevelReader level, BlockPos pos) {
        Direction.Axis outputAxis = output.getValue(BlockStateProperties.AXIS);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == outputAxis) continue;
            BlockPos enginePos = pos.relative(direction, 2);
            BlockState engine = level.getBlockState(enginePos);
            if (engine.getBlock() instanceof FurnaceEngineBlock
                && getOutputPos(engine, enginePos).equals(pos)
                && isOutputValid(engine, output)) return engine;
        }
        return null;
    }

    // The flank the crank swings over. The wheel is pushed clear of it and no kinetic output
    // leaves that way, so both rules have to read this one answer or they disagree.
    public static Direction getCrankSide(BlockState engine) {
        Direction facing = getFacing(engine);
        return facing.getAxis() == Direction.Axis.Y ? null : facing.getClockWise();
    }

    private static boolean hasOtherEngine(LevelReader level, BlockPos furnacePos, BlockPos currentPos) {
        for (Direction direction : Direction.values()) {
            BlockPos candidatePos = furnacePos.relative(direction);
            if (candidatePos.equals(currentPos)) continue;
            BlockState candidate = level.getBlockState(candidatePos);
            if (candidate.getBlock() instanceof FurnaceEngineBlock
                && getFurnacePos(candidate, candidatePos).equals(furnacePos)) return true;
        }
        return false;
    }

    private static boolean isFurnaceFront(BlockState furnace, BlockPos furnacePos, BlockPos enginePos) {
        return furnace.hasProperty(HorizontalDirectionalBlock.FACING)
            && furnacePos.relative(furnace.getValue(HorizontalDirectionalBlock.FACING)).equals(enginePos);
    }

    private static class PlacementHelper implements IPlacementHelper {
        @Override public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.FLYWHEEL::isIn;
        }
        @Override public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof FurnaceEngineBlock;
        }

        @Override public PlacementOffset getOffset(Player player, Level level, BlockState state,
            BlockPos pos, BlockHitResult hit) {
            BlockPos outputPos = getOutputPos(state, pos);
            if (!level.getBlockState(outputPos).canBeReplaced()) return PlacementOffset.fail();

            BlockState ghost = AllBlocks.FLYWHEEL.getDefaultState();

            Direction.Axis axis = null;
            for (Direction direction : Direction.orderedByNearest(player)) {
                if (isOutputValid(state, ghost.setValue(BlockStateProperties.AXIS, direction.getAxis()))) {
                    axis = direction.getAxis();
                    break;
                }
            }
            if (axis == null) return PlacementOffset.fail();

            Direction.Axis placedAxis = axis;
            BlockState placedState = level.isClientSide ? ghost
                : ModBlocks.POWERED_FLYWHEEL.get().defaultBlockState();
            return PlacementOffset.success(outputPos, placed -> BlockHelper.copyProperties(placed, placedState)
                .setValue(BlockStateProperties.AXIS, placedAxis));
        }
    }

    @Override public Class<FurnaceEngineBlockEntity> getBlockEntityClass() { return FurnaceEngineBlockEntity.class; }
    @Override public BlockEntityType<? extends FurnaceEngineBlockEntity> getBlockEntityType() { return ModBlockEntityTypes.FURNACE_ENGINE.get(); }
    @Override protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() { return CODEC; }
}
