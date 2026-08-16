package me.moonscenty.createfurnaceengine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import me.moonscenty.createfurnaceengine.content.PoweredShaftBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class PoweredShaftRenderer extends SafeBlockEntityRenderer<PoweredShaftBlockEntity> {
    public PoweredShaftRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(PoweredShaftBlockEntity be, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffer, int light, int overlay) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        SuperByteBuffer wheel = CachedBuffers.block(be.getBlockState());
        KineticBlockEntityRenderer.standardKineticRotationTransform(wheel, be, light).renderInto(poseStack, consumer);
    }
}
