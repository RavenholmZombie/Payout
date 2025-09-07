package com.rz.payout;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod(Payout.MODID)
public class Payout
{
    public static final String MODID = "payout";
    private static long lastRunTime = System.currentTimeMillis();

    public Payout()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PayoutConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLoad(final ModConfigEvent.Loading event)
    {
        if (event.getConfig().getSpec() == PayoutConfig.SPEC)
        {
            PayoutConfig.attach(event.getConfig());
        }
    }

    @SubscribeEvent
    public void onReload(final ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getSpec() == PayoutConfig.SPEC)
        {
            PayoutConfig.attach(event.getConfig());
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        PayoutCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;
        long now = System.currentTimeMillis();
        if (now - lastRunTime >= PayoutConfig.intervalMillis())
        {
            rewardAllPlayers(event.getServer());
            lastRunTime = now;
        }
    }

    public static void rewardAllPlayers(MinecraftServer server)
    {
        ItemStack reward = PayoutConfig.rewardItem();
        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            player.getInventory().add(reward.copy());
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "================================\n" +
                            "You got paid.\n" +
                            "Amount: " + reward.getCount() + " " + reward.getDisplayName().getString() + "\n" +
                            "Description: Thank you for playing on RZCraft!\n" +
                            "================================"
            ));
        }
    }

    // Hooks for commands
    public static void setReward(ItemStack stack)
    {
        PayoutConfig.setReward(stack);
    }

    public static void setInterval(long millis)
    {
        PayoutConfig.setInterval(millis);
    }

    public static void triggerNow(MinecraftServer server)
    {
        rewardAllPlayers(server);
        lastRunTime = System.currentTimeMillis();
    }

    public static long getLastRunTime()
    {
        return lastRunTime;
    }
}
