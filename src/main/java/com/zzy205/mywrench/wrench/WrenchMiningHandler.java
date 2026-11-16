package com.zzy205.mywrench.wrench;

import com.zzy205.mywrench.Config;
import com.zzy205.mywrench.MyWrench;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 让仍处于 create:wrench 身份下的扳手变成「万能挖掘工具」。
 * <p>
 * 掉落判定：{@code ServerPlayerGameMode#destroyBlock} → {@code BlockState#canHarvestBlock}
 * → NeoForge 的 {@code IBlockExtension#canHarvestBlock} → 触发 {@link PlayerEvent.HarvestCheck}，
 * 因此把 canHarvest 设成 true 就能让需要「正确工具」的方块也正常掉落。
 * <p>
 * 挖掘速度：{@code Player#getDigSpeed} 在算完药水/水下/悬空等倍率后触发
 * {@link PlayerEvent.BreakSpeed}。扳手不是工具，基础速度是 1.0，
 * 所以按倍数放大后，x8 约等于钻石工具的挖掘速度，同时保留原版的各种惩罚。
 * <p>
 * 效率附魔：原版 {@code Player#getDigSpeed} 里只会在 {@code f > 1.0F} 时把
 * {@link Attributes#MINING_EFFICIENCY}（效率附魔给的属性，量 = 等级²+1）加进基础速度。
 * 扳手没有 {@code DataComponents.TOOL} 组件，基础速度恒为 1.0，永远进不了那个分支，
 * 于是效率附魔完全无效。这里按原版语义自己补上：
 * {@code 新速度 = (虚拟工具速度 + 效率属性值) × (药水/水下/悬空等倍率之积)}。
 */
@EventBusSubscriber(modid = MyWrench.MODID)
public final class WrenchMiningHandler {

    private WrenchMiningHandler() {
    }

    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (!Config.SPEC.isLoaded() || !Config.ENABLE_WRENCH_MINING.getAsBoolean()
                || !Config.ENABLE_WRENCH_MINING_DROPS.getAsBoolean())
            return;

        if (!isHoldingWrench(event.getEntity()))
            return;

        event.setCanHarvest(true);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!Config.SPEC.isLoaded() || !Config.ENABLE_WRENCH_MINING.getAsBoolean())
            return;

        Player player = event.getEntity();
        if (!isHoldingWrench(player))
            return;

        float multiplier = (float) Config.WRENCH_MINING_SPEED_MULTIPLIER.getAsDouble();
        ItemStack stack = player.getMainHandItem();
        BlockState state = event.getState();
        float toolBase = stack.getDestroySpeed(state);

        if (toolBase > 1.0F) {
            // 扳手将来若真带上了 TOOL 组件，原版已经把效率算进基础速度了，
            // 这里只按倍数放大，避免效率被算两次。
            event.setNewSpeed(event.getOriginalSpeed() * multiplier);
            return;
        }

        // originalSpeed = 基础速度(1.0) × K，K 是药水/水下/悬空等倍率之积
        float k = event.getOriginalSpeed();
        double efficiency = player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        event.setNewSpeed((float) ((multiplier + efficiency) * k));
    }

    private static boolean isHoldingWrench(Player player) {
        return player != null && WrenchUpgrades.isUpgradedWrench(player.getMainHandItem());
    }
}
