package me.moonscenty.createfurnaceengine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlock;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlockEntity;
import me.moonscenty.createfurnaceengine.content.PoweredBlockEntity;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceEngineRenderer extends SafeBlockEntityRenderer<FurnaceEngineBlockEntity> {
    public FurnaceEngineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(FurnaceEngineBlockEntity be, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = FurnaceEngineBlock.getFacing(state);
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());

        renderLid(be, state, facing, light, poseStack, consumer);

        PoweredBlockEntity output = be.getOutput();
        if (output == null) return;

        Float targetAngle = be.getTargetAngle();
        if (targetAngle == null) return;
        float angle = targetAngle;
        boolean roll90 = facing.getAxis().isHorizontal()
            && KineticBlockEntityRenderer.getRotationAxisOf(output) == Direction.Axis.Y;
        float sine = Mth.sin(angle);
        float piston = ((1 - sine) / 4) * 24 / 16f;
        transformed(ModPartialModels.ENGINE_PISTON, state, facing, roll90)
            .translate(0, piston, 0).light(light).renderInto(poseStack, consumer);
        transformed(ModPartialModels.ENGINE_LINKAGE, state, facing, roll90)
            .center().translate(0, 1, 0).uncenter().translate(0, piston, 0)
            .translate(0, 4 / 16f, 8 / 16f).rotateXDegrees(Mth.sin(angle - Mth.HALF_PI) * 23f)
            .translate(0, -4 / 16f, -8 / 16f).light(light).renderInto(poseStack, consumer);
        transformed(ModPartialModels.ENGINE_CRANK, state, facing, roll90)
            .translate(0, 2, 0).center().rotateX(-angle + Mth.HALF_PI).uncenter()
            .light(light).renderInto(poseStack, consumer);
    }

    // The lid dresses the furnace rather than the engine, so it is drawn a block over. It is
    // modelled a pixel proud of that block on every side it covers, which is what keeps it clear
    // of the furnace's own faces instead of z-fighting with them.
    //
    // Nothing here waits on the engine running. The lid is part of the installation and belongs
    // on the furnace from the moment the engine is placed.
    private void renderLid(FurnaceEngineBlockEntity be, BlockState state, Direction facing, int light,
        PoseStack poseStack, VertexConsumer consumer) {
        Level level = be.getLevel();
        if (level == null) return;

        // A furnace is a full block and swallows all light, so the sample is taken from the space
        // above it where the lid's plate actually sits. Falling back to the engine's own light
        // keeps the lid off pitch black when that space is boxed in as well.
        BlockPos furnacePos = FurnaceEngineBlock.getFurnacePos(state, be.getBlockPos());
        int lidLight = SuperByteBuffer.maxLight(light, LevelRenderer.getLightColor(level, furnacePos.above()));

        CachedBuffers.partial(ModPartialModels.ENGINE_LID, state)
            .translate(facing.getOpposite().getNormal())
            .light(lidLight).renderInto(poseStack, consumer);
    }

    private SuperByteBuffer transformed(PartialModel model, BlockState state, Direction facing, boolean roll90) {
        return CachedBuffers.partial(model, state).center()
            .rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
            .rotateYDegrees(roll90 ? -90 : 0).uncenter();
    }

    @Override public int getViewDistance() { return 128; }
}
