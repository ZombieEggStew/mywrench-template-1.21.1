package com.zzy205.mywrench.wrench;

import com.simibubi.create.AllItems;
import com.zzy205.mywrench.Config;
import com.zzy205.mywrench.MyWrenchDataComponents;

import net.minecraft.world.item.ItemStack;

/**
 * 判断某个 ItemStack 是不是「已经锻造强化过的 create:wrench」。
 */
public final class WrenchUpgrades {

    private WrenchUpgrades() {
    }

    public static boolean isUpgradedWrench(ItemStack stack) {
        if (!stack.is(AllItems.WRENCH.get()))
            return false;
        if (!Config.SPEC.isLoaded())
            return false;
        if (!Config.ENABLE_UPGRADE_GATE.getAsBoolean())
            return true;
        return stack.has(MyWrenchDataComponents.WRENCH_UPGRADED.get());
    }
}
