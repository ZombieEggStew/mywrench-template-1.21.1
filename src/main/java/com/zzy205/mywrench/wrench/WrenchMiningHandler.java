package com.zzy205.mywrench.wrench;

import com.zzy205.mywrench.Config;
import com.zzy205.mywrench.MyWrench;

import net.minecraft.world.entity.player.Player;
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

        if (!isHoldingWrench(event.getEntity()))
            return;

        float multiplier = (float) Config.WRENCH_MINING_SPEED_MULTIPLIER.getAsDouble();
        event.setNewSpeed(event.getOriginalSpeed() * multiplier);
    }

    private static boolean isHoldingWrench(Player player) {
        return player != null && WrenchUpgrades.isUpgradedWrench(player.getMainHandItem());
    }
}
