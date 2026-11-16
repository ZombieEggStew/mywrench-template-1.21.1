package com.zzy205.mywrench;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MyWrench.MODID)
public class MyWrench {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mywrench";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "mywrench" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "mywrench" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // Creates a new Block with the id "mywrench:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "mywrench:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // 锻造模板 tooltip 的颜色：#a8a8a8 用于升级标题（Attack Upgrade / Mining Upgrade），#5454fc 用于「Create Wrench」/「Netherite Ingot」
    private static final TextColor TEMPLATE_TITLE_COLOR = TextColor.fromRgb(0xA8A8A8);
    private static final TextColor TEMPLATE_DESCRIPTION_COLOR = TextColor.fromRgb(0x5454FC);

    /**
     * 扳手攻击升级模板（贴图 wrench_upgrade_smithing_template_1）：
     * 锻造台 = 本模板 + create:wrench + 下界合金锭 → 扳手获得高额攻击力（wrench_attack_upgrade 组件）。
     */
    public static final DeferredItem<SmithingTemplateItem> WRENCH_ATTACK_UPGRADE_SMITHING_TEMPLATE = ITEMS.register(
            "wrench_attack_upgrade_smithing_template",
            () -> createTemplate(
                    "wrench_attack_upgrade",
                    ResourceLocation.withDefaultNamespace("item/empty_slot_sword")));

    /**
     * 扳手工具升级模板（贴图 wrench_upgrade_smithing_template_2）：
     * 锻造台 = 本模板 + create:wrench + 下界合金锭 → 扳手获得钻石级挖掘能力（wrench_mining_upgrade 组件）。
     */
    public static final DeferredItem<SmithingTemplateItem> WRENCH_MINING_UPGRADE_SMITHING_TEMPLATE = ITEMS.register(
            "wrench_mining_upgrade_smithing_template",
            () -> createTemplate(
                    "wrench_mining_upgrade",
                    ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe")));

    private static SmithingTemplateItem createTemplate(String descriptionKey, ResourceLocation baseSlotIcon) {
        return new SmithingTemplateItem(
                Component.translatable("item.mywrench.smithing_template." + descriptionKey + ".applies_to")
                        .withStyle(style -> style.withColor(TEMPLATE_DESCRIPTION_COLOR)),
                Component.translatable("item.mywrench.smithing_template." + descriptionKey + ".ingredients")
                        .withStyle(style -> style.withColor(TEMPLATE_DESCRIPTION_COLOR)),
                Component.translatable("item.mywrench.smithing_template." + descriptionKey + ".upgrade")
                        .withStyle(style -> style.withColor(TEMPLATE_TITLE_COLOR)),
                Component.translatable("item.mywrench.smithing_template." + descriptionKey + ".base_slot_description"),
                Component.translatable("item.mywrench.smithing_template." + descriptionKey + ".additions_slot_description"),
                List.of(baseSlotIcon),
                List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot")));
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MyWrench(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so data components get registered
        MyWrenchDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (MyWrench) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // 把锻造模板放进原版创造模式标签页
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(WRENCH_ATTACK_UPGRADE_SMITHING_TEMPLATE);
            event.accept(WRENCH_MINING_UPGRADE_SMITHING_TEMPLATE);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
