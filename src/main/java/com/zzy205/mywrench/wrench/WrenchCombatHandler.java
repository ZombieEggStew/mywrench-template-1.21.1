package com.zzy205.mywrench.wrench;

import com.simibubi.create.AllItems;
import com.zzy205.mywrench.Config;
import com.zzy205.mywrench.MyWrench;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * 给仍处于 create:wrench 身份下的扳手补上武器属性。
 * <p>
 * {@link ItemAttributeModifierEvent} 在每次查询 {@code ItemStack#getAttributeModifiers()} 时触发
 * （NeoForge 的 IItemStackExtension 实现），因此不需要替换物品，就能让原版扳手获得
 * 攻击伤害 / 攻击速度，tooltip 也会自动跟随显示。
 * <p>
 * 修饰符 id 用原版的 {@link Item#BASE_ATTACK_DAMAGE_ID} / {@link Item#BASE_ATTACK_SPEED_ID}：
 * 只有 id 等于 {@code Attribute#getBaseId()} 时，NeoForge 才会把它当作「武器基础属性」，
 * 与玩家基础值合并显示成「8 Attack Damage」（高级 tooltip 里再显示「[1 + 7]」）。
 */
@EventBusSubscriber(modid = MyWrench.MODID)
public final class WrenchCombatHandler {

    /** 玩家基础攻击伤害，即 Attributes.ATTACK_DAMAGE 的默认值。 */
    private static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0D;

    /** 玩家基础攻击速度，即 Attributes.ATTACK_SPEED 的默认值。 */
    private static final double PLAYER_BASE_ATTACK_SPEED = 4.0D;

    private WrenchCombatHandler() {
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (!Config.SPEC.isLoaded() || !Config.ENABLE_WRENCH_COMBAT.getAsBoolean())
            return;

        ItemStack stack = event.getItemStack();
        if (!stack.is(AllItems.WRENCH.get()))
            return;

        double damageBonus = Config.WRENCH_ATTACK_DAMAGE.getAsDouble() - PLAYER_BASE_ATTACK_DAMAGE;
        if (damageBonus != 0.0D) {
            event.addModifier(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damageBonus, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }

        double speedAmount = Config.WRENCH_ATTACK_SPEED.getAsDouble() - PLAYER_BASE_ATTACK_SPEED;
        if (speedAmount != 0.0D) {
            event.addModifier(Attributes.ATTACK_SPEED,
                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speedAmount, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
    }
}
