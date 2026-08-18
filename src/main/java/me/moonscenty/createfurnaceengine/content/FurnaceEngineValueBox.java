package me.moonscenty.createfurnaceengine.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.Pointing;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Places the rotation-direction scroll box on the engine. Create's SteamEngineValueBox is tuned
 * for its own head model and carries branches for engines pointing up or down, which this block
 * no longer allows, so the position is owned here instead.
 */
public class FurnaceEngineValueBox extends ValueBoxTransform.Sided {
    /**
     * Where the box sits before it is rolled onto the flank it lives on, in voxels of a
     * south-facing engine. X and Y orbit the block centre, so 8 is the axis and the distance from
     * it decides how far the box floats off the head; Z runs along the direction the engine
     * extends. Nudge this to follow the head model.
     */
    private static final Vec3 OFFSET = VecHelper.voxelSpace(8, 14.5, 9);

    /**
     * How far the head model sits off the block axis, in voxels. The whole assembly is shifted
     * this way so the piston, linkage and crank line up with the rim of a flywheel that was
     * itself pushed the other way. The box has to ride along, otherwise it orbits the block
     * centre while the head it should be resting on is somewhere else. Keep in step with
     * block/furnace_engine/head.json.
     */
    private static final double HEAD_OFFSET = 4;

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        Direction facing = FurnaceEngineBlock.getFacing(state);
        if (facing.getAxis() == side.getAxis())
            return false;
        // The two rolls that land flat against the head would sink the box into it.
        if (roll(facing, side) % 180 == 0)
            return false;
        // Of the two flanks left, the crank sweeps over one of them. A box there would sit in the
        // path of the moving arm, so the engine offers the setting on the clear flank only.
        return side != FurnaceEngineBlock.getCrankSide(state);
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction facing = FurnaceEngineBlock.getFacing(state);
        Vec3 local = VecHelper.rotateCentered(OFFSET, roll(facing, getSide()), Axis.Z);
        // Engines only mount on a furnace's side faces, so the vertical angle is always zero and
        // the X rotation Create applies for up/down facings is not needed here.
        local = VecHelper.rotateCentered(local, AngleHelper.horizontalAngle(facing), Axis.Y);

        // Slide across to the head, which shares its flank with the crank.
        Direction lateral = FurnaceEngineBlock.getCrankSide(state);
        if (lateral == null)
            return local;
        return local.add(Vec3.atLowerCornerOf(lateral.getNormal()).scale(HEAD_OFFSET / 16));
    }

    @Override
    public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
        Direction facing = FurnaceEngineBlock.getFacing(state);
        TransformStack.of(ms)
            .rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(90)
            .rotateYDegrees(roll(facing, getSide()));
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }

    private static float roll(Direction facing, Direction side) {
        for (Pointing pointing : Pointing.values())
            if (pointing.getCombinedDirection(facing) == side)
                return pointing.getXRotation();
        return 0;
    }
}
