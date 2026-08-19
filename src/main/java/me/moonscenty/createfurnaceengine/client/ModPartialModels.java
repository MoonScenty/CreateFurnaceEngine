package me.moonscenty.createfurnaceengine.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import me.moonscenty.createfurnaceengine.CreateFurnaceEngine;
import net.minecraft.resources.ResourceLocation;

public final class ModPartialModels {
    public static final PartialModel ENGINE_PISTON = block("furnace_engine/piston");
    public static final PartialModel ENGINE_LINKAGE = block("furnace_engine/linkage");
    public static final PartialModel ENGINE_CRANK = block("furnace_engine/crank");
    public static final PartialModel ENGINE_LID = block("furnace_engine/lid");

    private ModPartialModels() {}

    public static void init() {}

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(
            CreateFurnaceEngine.MODID, "block/" + path));
    }
}
