package me.moonscenty.createfurnaceengine;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import me.moonscenty.createfurnaceengine.registry.ModBlockEntityTypes;
import me.moonscenty.createfurnaceengine.registry.ModBlocks;
import me.moonscenty.createfurnaceengine.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;

@Mod(CreateFurnaceEngine.MODID)
public class CreateFurnaceEngine {
    public static final String MODID = "createfurnaceengine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateFurnaceEngine(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntityTypes.register(modEventBus);
        modEventBus.addListener(this::addToCreativeTab);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS)
            event.accept(ModItems.FURNACE_ENGINE);
    }
}
