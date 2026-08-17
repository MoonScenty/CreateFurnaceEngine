package me.moonscenty.createfurnaceengine.ponder;

import me.moonscenty.createfurnaceengine.CreateFurnaceEngine;
import me.moonscenty.createfurnaceengine.registry.ModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class FurnaceEnginePonderPlugin implements PonderPlugin {
    @Override public String getModId() { return CreateFurnaceEngine.MODID; }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModBlocks.FURNACE_ENGINE.getId())
            .addStoryBoard("furnace_engine", FurnaceEngineScenes::placement)
            .addStoryBoard("furnace_engine", FurnaceEngineScenes::shaftAndDirection)
            .addStoryBoard("furnace_engine", FurnaceEngineScenes::operationAndHeatSink);
    }
}
