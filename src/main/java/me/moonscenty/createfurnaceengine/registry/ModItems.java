package me.moonscenty.createfurnaceengine.registry;

import me.moonscenty.createfurnaceengine.CreateFurnaceEngine;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateFurnaceEngine.MODID);
    public static final DeferredItem<BlockItem> FURNACE_ENGINE = ITEMS.registerSimpleBlockItem(ModBlocks.FURNACE_ENGINE);
    private ModItems() {}
    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
