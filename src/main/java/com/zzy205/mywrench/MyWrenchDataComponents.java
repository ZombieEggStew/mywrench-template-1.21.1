package com.zzy205.mywrench;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 本模组用到的数据组件。
 * <p>
 * 两个布尔标记分别代表两种锻造升级：
 * <ul>
 *     <li>{@code mywrench:wrench_attack_upgrade} —— 攻击升级模板打的标记，开启高额攻击力。</li>
 *     <li>{@code mywrench:wrench_mining_upgrade} —— 工具升级模板打的标记，开启钻石级挖掘。</li>
 * </ul>
 * 用组件而不是新物品做标记，是为了让物品 id 保持 {@code create:wrench}，
 * 从而不破坏 Create 里约 30 处 {@code AllItems.WRENCH.isIn(...)} 身份判定。
 * 两个标记可以同时存在（先打一个模板再打另一个），此时两种能力都生效。
 */
public final class MyWrenchDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MyWrench.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WRENCH_ATTACK_UPGRADE =
            DATA_COMPONENT_TYPES.register("wrench_attack_upgrade", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WRENCH_MINING_UPGRADE =
            DATA_COMPONENT_TYPES.register("wrench_mining_upgrade", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    private MyWrenchDataComponents() {
    }
}
