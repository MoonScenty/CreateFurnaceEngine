package me.moonscenty.createfurnaceengine.client;

import me.moonscenty.createfurnaceengine.CreateFurnaceEngine;
import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CreateFurnaceEngine.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModPartialModels.init();
        event.registerBlockEntityRenderer(ModBlockEntityTypes.POWERED_SHAFT.get(), PoweredShaftRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.FURNACE_ENGINE.get(), FurnaceEngineRenderer::new);
    }
}
