package me.moonscenty.createfurnaceengine.registry;

import java.util.function.Supplier;
import me.moonscenty.createfurnaceengine.CreateFurnaceEngine;
import me.moonscenty.createfurnaceengine.content.FurnaceEngineBlockEntity;
import me.moonscenty.createfurnaceengine.content.PoweredShaftBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateFurnaceEngine.MODID);
    public static final Supplier<BlockEntityType<FurnaceEngineBlockEntity>> FURNACE_ENGINE = TYPES.register("furnace_engine",
        () -> BlockEntityType.Builder.of(FurnaceEngineBlockEntity::new, ModBlocks.FURNACE_ENGINE.get()).build(null));
    public static final Supplier<BlockEntityType<PoweredShaftBlockEntity>> POWERED_SHAFT = TYPES.register("powered_shaft",
        () -> BlockEntityType.Builder.of(PoweredShaftBlockEntity::new, ModBlocks.POWERED_SHAFT.get()).build(null));
    private ModBlockEntityTypes() {}
    public static void register(IEventBus bus) { TYPES.register(bus); }
}
