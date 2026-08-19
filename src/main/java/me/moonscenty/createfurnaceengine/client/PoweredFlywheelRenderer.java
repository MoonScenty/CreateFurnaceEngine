package me.moonscenty.createfurnaceengine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlock;
import me.moonscenty.createfurnaceengine.content.PoweredFlywheelBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// Deliberately not a KineticBlockEntityRenderer: that one bails out whenever the Flywheel
// backend is active, which would leave this block invisible until a Visual is registered too.
public class PoweredFlywheelRenderer extends SafeBlockEntityRenderer<PoweredFlywheelBlockEntity> {
    // All values are in model pixels, the same units Blockbench shows: 16 per block.

    // Measured from flywheel_shaftless.obj, which spans 3..13 along its axis.
    private static final float WHEEL_THICKNESS = 10f;

    // How far the axle shows past the wheel, at both ends.
    private static final float AXLE_OVERHANG = 0.5f;

    // How far the wheel sits off centre. 3 parks it flush against one face.
    private static final float WHEEL_OFFSET = 3f;

    private static final float PIXEL = 1 / 16f;

    public PoweredFlywheelRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(PoweredFlywheelBlockEntity be, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffer, int light, int overlay) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(be);
        float offset = WHEEL_OFFSET * PIXEL * clearanceSign(be, axis);

        // The axle hugs the wheel rather than spanning the block, so both ends show the same
        // amount. Scaling along the rotation axis cannot disturb a rotation about that axis,
        // and one stretched model leaves no seam for two overlapping copies to fight over.
        float stretch = (WHEEL_THICKNESS + 2f * AXLE_OVERHANG) / 16f;
        SuperByteBuffer shaft = CachedBuffers.block(KineticBlockEntityRenderer.shaft(axis));
        axial(KineticBlockEntityRenderer.standardKineticRotationTransform(shaft, be, light), axis, offset)
            .center()
            .scale(axis == Axis.X ? stretch : 1, axis == Axis.Y ? stretch : 1, axis == Axis.Z ? stretch : 1)
            .uncenter()
            .renderInto(poseStack, consumer);

        SuperByteBuffer wheel = CachedBuffers.block(be.getBlockState());
        axial(KineticBlockEntityRenderer.standardKineticRotationTransform(wheel, be, light), axis, offset)
            .renderInto(poseStack, consumer);
    }

    // The engine's crank is placed in the engine's own frame, so it swaps sides when the engine
    // is turned around. The wheel has to follow, or it lands on top of the crank instead of
    // leaving the half of the block that the crank reaches into.
    private static float clearanceSign(PoweredFlywheelBlockEntity be, Axis axis) {
        Level level = be.getLevel();
        if (level == null) return 1;
        BlockState engine = drivingEngine(be, level);
        if (engine == null) return 1;

        Direction crank = FurnaceEngineBlock.getCrankSide(engine);
        if (crank == null || crank.getAxis() != axis) return 1;

        Direction clear = crank.getOpposite();
        return clear.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1;
    }

    // The engine's claim is the authority on which engine drives this wheel, but it is only ever
    // written server-side. Ponder runs entirely on the client, so there the claim is never set and
    // the wheel would fall back to an unshifted position that parks it right on top of the crank.
    // Reading the neighbourhood instead costs one lookup and answers in any level.
    private static BlockState drivingEngine(PoweredFlywheelBlockEntity be, Level level) {
        BlockPos enginePos = be.getEnginePos();
        if (enginePos != null) {
            BlockState claimed = level.getBlockState(enginePos);
            if (claimed.getBlock() instanceof FurnaceEngineBlock) return claimed;
        }
        return FurnaceEngineBlock.findEngine(be.getBlockState(), level, be.getBlockPos());
    }

    // Must be applied before any scaling, or the scale would shrink the offset along with it.
    private static SuperByteBuffer axial(SuperByteBuffer model, Axis axis, float distance) {
        return model.translate(axis == Axis.X ? distance : 0,
                               axis == Axis.Y ? distance : 0,
                               axis == Axis.Z ? distance : 0);
    }

    @Override public int getViewDistance() { return 128; }
}
