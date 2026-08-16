package me.moonscenty.createfurnaceengine.registry;

import me.moonscenty.createfurnaceengine.CreateFurnaceEngine;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlock;
import me.moonscenty.createfurnaceengine.content.PoweredShaftBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateFurnaceEngine.MODID);
    public static final DeferredBlock<FurnaceEngineBlock> FURNACE_ENGINE = BLOCKS.registerBlock("furnace_engine",
        FurnaceEngineBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<PoweredShaftBlock> POWERED_SHAFT = BLOCKS.register("powered_shaft",
        () -> new PoweredShaftBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW)
            .strength(3).sound(SoundType.METAL).noOcclusion()));

    private ModBlocks() {}
    public static void register(IEventBus bus) { BLOCKS.register(bus); }
}
