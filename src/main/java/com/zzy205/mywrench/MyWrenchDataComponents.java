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
 * {@code mywrench:wrench_upgraded}：布尔标记，表示这把 {@code create:wrench} 已经在锻造台上
 * 用「扳手升级模板 + 下界合金锭」强化过。用它做标记而不是新物品，是为了让物品 id 保持
 * {@code create:wrench}，从而不破坏 Create 里约 30 处 {@code AllItems.WRENCH.isIn(...)} 身份判定。
 */
public final class MyWrenchDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MyWrench.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WRENCH_UPGRADED =
            DATA_COMPONENT_TYPES.register("wrench_upgraded", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    private MyWrenchDataComponents() {
    }
}
