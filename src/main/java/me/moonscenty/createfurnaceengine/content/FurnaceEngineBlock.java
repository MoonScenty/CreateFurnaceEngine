package me.moonscenty.createfurnaceengine.content;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.BlockHelper;
import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import me.moonscenty.createfurnaceengine.registry.ModBlocks;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
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
        BlockPos furnacePos = getFurnacePos(state, pos);
        BlockState furnace = level.getBlockState(furnacePos);
        return furnace.getBlock() instanceof AbstractFurnaceBlock
            && !isFurnaceFront(furnace, furnacePos, pos)
            && !hasOtherEngine(level, furnacePos, pos);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
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
        connectShaft(level, state, pos);
    }

    static void connectShaft(Level level, BlockState state, BlockPos pos) {
        BlockPos shaftPos = getShaftPos(state, pos);
        BlockState shaft = level.getBlockState(shaftPos);
        if (isShaftValid(state, shaft)) level.setBlock(shaftPos, PoweredShaftBlock.getEquivalent(shaft), 3);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            BlockPos shaftPos = getShaftPos(state, pos);
            BlockState shaft = level.getBlockState(shaftPos);
            if (shaft.is(ModBlocks.POWERED_SHAFT.get()))
                level.setBlock(shaftPos, PoweredShaftBlock.getUnpoweredEquivalent(shaft), 3);
        }
        IBE.onRemove(state, level, pos, replacement);
    }

    @Override public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
        BlockPos pos, CollisionContext context) {
        AttachFace face = state.getValue(FACE); Direction direction = state.getValue(FACING);
        return face == AttachFace.CEILING ? AllShapes.STEAM_ENGINE_CEILING.get(direction.getAxis())
            : face == AttachFace.FLOOR ? AllShapes.STEAM_ENGINE.get(direction.getAxis())
            : AllShapes.STEAM_ENGINE_WALL.get(direction);
    }

    public static Direction getFacing(BlockState state) { return getConnectedDirection(state); }
    public static BlockPos getFurnacePos(BlockState state, BlockPos pos) { return pos.relative(getFacing(state).getOpposite()); }
    public static BlockPos getShaftPos(BlockState state, BlockPos pos) { return pos.relative(getFacing(state), 2); }
    public static boolean isShaftValid(BlockState engine, BlockState shaft) {
        return (AllBlocks.SHAFT.has(shaft) || shaft.is(ModBlocks.POWERED_SHAFT.get()))
            && shaft.getValue(ShaftBlock.AXIS) != getFacing(engine).getAxis();
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
        @Override public Predicate<ItemStack> getItemPredicate() { return AllBlocks.SHAFT::isIn; }
        @Override public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof FurnaceEngineBlock;
        }

        @Override public PlacementOffset getOffset(Player player, Level level, BlockState state,
            BlockPos pos, BlockHitResult hit) {
            BlockPos shaftPos = getShaftPos(state, pos);
            BlockState shaft = AllBlocks.SHAFT.getDefaultState();
            for (Direction direction : Direction.orderedByNearest(player)) {
                shaft = shaft.setValue(ShaftBlock.AXIS, direction.getAxis());
                if (isShaftValid(state, shaft)) break;
            }
            if (!level.getBlockState(shaftPos).canBeReplaced()) return PlacementOffset.fail();

            Direction.Axis axis = shaft.getValue(ShaftBlock.AXIS);
            return PlacementOffset.success(shaftPos, placed -> BlockHelper.copyProperties(placed,
                (level.isClientSide ? AllBlocks.SHAFT.getDefaultState()
                    : ModBlocks.POWERED_SHAFT.get().defaultBlockState()))
                .setValue(PoweredShaftBlock.AXIS, axis));
        }
    }

    @Override public Class<FurnaceEngineBlockEntity> getBlockEntityClass() { return FurnaceEngineBlockEntity.class; }
    @Override public BlockEntityType<? extends FurnaceEngineBlockEntity> getBlockEntityType() { return ModBlockEntityTypes.FURNACE_ENGINE.get(); }
    @Override protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() { return CODEC; }
}
