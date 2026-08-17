package me.moonscenty.createfurnaceengine.client;

import me.moonscenty.createfurnaceengine.CreateFurnaceEngine;
import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.createmod.ponder.foundation.PonderIndex;
import me.moonscenty.createfurnaceengine.ponder.FurnaceEnginePonderPlugin;

@EventBusSubscriber(modid = CreateFurnaceEngine.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModPartialModels.init();
        event.registerBlockEntityRenderer(ModBlockEntityTypes.POWERED_SHAFT.get(), PoweredShaftRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.FURNACE_ENGINE.get(), FurnaceEngineRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new FurnaceEnginePonderPlugin()));
    }
}
