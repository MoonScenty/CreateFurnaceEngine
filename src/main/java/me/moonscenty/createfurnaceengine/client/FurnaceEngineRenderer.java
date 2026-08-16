package me.moonscenty.createfurnaceengine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlock;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlockEntity;
import me.moonscenty.createfurnaceengine.content.PoweredShaftBlockEntity;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceEngineRenderer extends SafeBlockEntityRenderer<FurnaceEngineBlockEntity> {
    public FurnaceEngineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(FurnaceEngineBlockEntity be, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = FurnaceEngineBlock.getFacing(state);
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());

        PoweredShaftBlockEntity shaft = be.getShaft();
        if (shaft == null) return;

        Float targetAngle = be.getTargetAngle();
        if (targetAngle == null) return;
        float angle = targetAngle;
        boolean roll90 = facing.getAxis().isHorizontal()
            && KineticBlockEntityRenderer.getRotationAxisOf(shaft) == Direction.Axis.Y;
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

    private SuperByteBuffer transformed(PartialModel model, BlockState state, Direction facing, boolean roll90) {
        return CachedBuffers.partial(model, state).center()
            .rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
            .rotateYDegrees(roll90 ? -90 : 0).uncenter();
    }

    @Override public int getViewDistance() { return 128; }
}
