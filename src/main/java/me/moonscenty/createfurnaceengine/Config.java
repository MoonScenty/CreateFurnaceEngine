package me.moonscenty.createfurnaceengine;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BASE_RPM = BUILDER
        .comment("Furnace Engine RPM when no heat sink is adjacent to the furnace")
        .defineInRange("baseRpm", 40, 1, 256);
    public static final ModConfigSpec.IntValue BASE_SU_PER_RPM = BUILDER
        .comment("Furnace Engine stress capacity per RPM when no heat sink is present")
        .defineInRange("baseSuPerRpm", 32, 1, 1024);
    public static final ModConfigSpec.ConfigValue<String> HEAT_SINK_BLOCK = BUILDER
        .comment("Block id recognized as a heat sink")
        .define("heatSinkBlock", "minecraft:copper_block", Config::isBlockId);
    public static final ModConfigSpec.IntValue HEAT_SINK_RPM = BUILDER
        .comment("Furnace Engine RPM when at least one heat sink is adjacent to the furnace")
        .defineInRange("heatSinkRpm", 32, 1, 256);
    public static final ModConfigSpec.IntValue HEAT_SINK_SU_PER_RPM = BUILDER
        .comment("Furnace Engine stress capacity per RPM when a heat sink is present")
        .defineInRange("heatSinkSuPerRpm", 32, 1, 1024);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    private static boolean isBlockId(Object value) {
        return value instanceof String id && ResourceLocation.tryParse(id) != null;
    }
}
