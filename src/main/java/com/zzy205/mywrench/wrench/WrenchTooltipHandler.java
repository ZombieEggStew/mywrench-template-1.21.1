package com.zzy205.mywrench.wrench;

import com.zzy205.mywrench.MyWrench;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 给「已加固」的 create:wrench 补一行说明文字（名字保持 Wrench 不变）。
 * <p>
 * {@link ItemTooltipEvent} 在 {@code ItemStack#getTooltipLines} 末尾触发，所以这一行会出现在
 * 属性行（主手攻击伤害/攻击速度）之后。
 */
@EventBusSubscriber(modid = MyWrench.MODID)
public final class WrenchTooltipHandler {

    private WrenchTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!WrenchUpgrades.isUpgradedWrench(stack))
            return;

        event.getToolTip().add(Component.translatable("item.mywrench.wrench_upgraded.desc")
                .withStyle(ChatFormatting.GOLD));
    }
}
