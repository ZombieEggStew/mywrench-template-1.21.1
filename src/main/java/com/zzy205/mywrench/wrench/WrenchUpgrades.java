package com.zzy205.mywrench.wrench;

import com.simibubi.create.AllItems;
import com.zzy205.mywrench.Config;
import com.zzy205.mywrench.MyWrenchDataComponents;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

/**
 * 判断某个 ItemStack 是不是「打了某种锻造升级的 create:wrench」。
 * <p>
 * 两种升级互相独立：先打攻击模板再打工具模板，两个组件会同时保留，两种能力都生效。
 */
public final class WrenchUpgrades {

    private WrenchUpgrades() {
    }

    /** 攻击升级（wrench_attack_upgrade 组件）：高额攻击力 / 剑级攻速。 */
    public static boolean hasAttackUpgrade(ItemStack stack) {
        return hasUpgrade(stack, MyWrenchDataComponents.WRENCH_ATTACK_UPGRADE.get());
    }

    /** 工具升级（wrench_tool_upgrade 组件）：钻石级工具速度（挖矿、砍树、铲土等一切方块）。 */
    public static boolean hasToolUpgrade(ItemStack stack) {
        return hasUpgrade(stack, MyWrenchDataComponents.WRENCH_TOOL_UPGRADE.get());
    }

    private static boolean hasUpgrade(ItemStack stack, DataComponentType<Boolean> component) {
        if (!stack.is(AllItems.WRENCH.get()))
            return false;
        if (!Config.SPEC.isLoaded())
            return false;
        // 测试用：关掉门槛时，任何 create:wrench 都算已升级
        if (!Config.ENABLE_UPGRADE_GATE.getAsBoolean())
            return true;
        return stack.has(component);
    }
}
