package com.zzy205.mywrench;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    // ==== 扳手强化：战斗 =========================================================

    public static final ModConfigSpec.BooleanValue ENABLE_WRENCH_COMBAT = BUILDER
            .comment("Give the Create wrench (create:wrench) weapon attributes.")
            .define("enableWrenchCombat", true);

    public static final ModConfigSpec.DoubleValue WRENCH_ATTACK_DAMAGE = BUILDER
            .comment("Total attack damage while holding the wrench (base 1.0 + this item's bonus).",
                    "The modifier amount is (this value - 1.0), so the default 8.0 shows as '8 Attack Damage'.")
            .defineInRange("wrenchAttackDamage", 8.0D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue WRENCH_ATTACK_SPEED = BUILDER
            .comment("Final attack speed while holding the wrench, in attacks per second.",
                    "Players have a base attack speed of 4.0, so the modifier is (this value - 4.0); the default 1.6 shows as '1.6 Attack Speed'.")
            .defineInRange("wrenchAttackSpeed", 1.6D, 0.1D, 4.0D);

    // ==== 扳手强化：挖掘 =========================================================

    public static final ModConfigSpec.BooleanValue ENABLE_WRENCH_MINING = BUILDER
            .comment("Let the Create wrench mine blocks with a boosted speed.")
            .define("enableWrenchMining", true);

    public static final ModConfigSpec.DoubleValue WRENCH_MINING_SPEED_MULTIPLIER = BUILDER
            .comment("Mining speed multiplier applied to the wrench.",
                    "A non-tool item has a base mining speed of 1.0, so 8.0 is roughly diamond tool speed.")
            .defineInRange("wrenchMiningSpeedMultiplier", 8.0D, 1.0D, 100.0D);

    public static final ModConfigSpec.BooleanValue ENABLE_WRENCH_MINING_DROPS = BUILDER
            .comment("Also allow the wrench to harvest drops from blocks that require a correct tool (stone, ores, ...).")
            .define("enableWrenchMiningDrops", true);

    // ==== 扳手强化：锻造门槛 =====================================================

    public static final ModConfigSpec.BooleanValue ENABLE_UPGRADE_GATE = BUILDER
            .comment("Require a smithing upgrade before the wrench gets its bonus:",
                    "attack upgrade = mywrench:wrench_attack_upgrade, mining upgrade = mywrench:wrench_mining_upgrade.",
                    "Set to false to boost every create:wrench (testing only).")
            .define("enableUpgradeGate", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
