package com.rz.payout;

import com.rz.payout.client.PayoutClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Payout.MODID)
public class Payout
{
    public static final String MODID = "payout";
    private static long lastRunTime = System.currentTimeMillis();
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;

    public Payout()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PayoutConfig.SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onConfigLoad);
        modBus.addListener(this::onConfigReload);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("[Payout] Welcome to Payout 1.0.0 by RavenholmZombie!");

        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        CHANNEL.registerMessage(
                0,
                PayoutToastMessage.class,
                PayoutToastMessage::encode,
                PayoutToastMessage::decode,
                PayoutToastMessage::handle
        );

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.rz.payout.client.PayoutClient.init());


    }

    private void onConfigLoad(final ModConfigEvent.Loading event)
    {
        if (event.getConfig().getSpec() == PayoutConfig.SPEC)
        {
            PayoutConfig.attach(event.getConfig());
        }
        LOGGER.info("[Payout] onConfigLoad called. payout-server.toml loaded.");
    }

    private void onConfigReload(final ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getSpec() == PayoutConfig.SPEC)
        {
            PayoutConfig.attach(event.getConfig());
        }
        LOGGER.info("[Payout] onConfigReload called. payout-server.toml reloaded.");
    }

    @SubscribeEvent
    public void onLoad(final ModConfigEvent.Loading event)
    {
        if (event.getConfig().getSpec() == PayoutConfig.SPEC)
        {
            PayoutConfig.attach(event.getConfig());
            LOGGER.info("[Payout] onLoad Event");
        }
    }

    @SubscribeEvent
    public void onReload(final ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getSpec() == PayoutConfig.SPEC)
        {
            PayoutConfig.attach(event.getConfig());
        }
        LOGGER.info("[Payout] onReload Event");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        PayoutCommands.register(event.getDispatcher(), event.getBuildContext());
        LOGGER.info("[Payout] Commands registered.");
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

    public static void rewardAllPlayers(MinecraftServer server) {
        if (server.getPlayerList().getPlayerCount() == 0) {
            LOGGER.info("[Payout] Reward issuance skipped - No players online.");
            return;
        }

        ItemStack reward = PayoutConfig.rewardItem();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Give the reward
            player.getInventory().add(reward.copy());

            if (PayoutConfig.showToast())
            {
                Payout.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new PayoutToastMessage(
                                PayoutConfig.toastTitle(),
                                PayoutConfig.toastDescription(),
                                reward.copy()
                        )
                );
            }

            // Show actionbar message
            player.displayClientMessage(
                    Component.literal("You got paid: ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(reward.getCount() + "x " + reward.getDisplayName().getString())
                                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)),
                    true // true = actionbar
            );

            // Play optional sound
            if (PayoutConfig.playPayoutSound()) {
                ResourceLocation soundId = new ResourceLocation(PayoutConfig.payoutSound());
                player.level().playSound(
                        null,
                        player.blockPosition(),
                        ForgeRegistries.SOUND_EVENTS.getValue(soundId),
                        SoundSource.PLAYERS,
                        PayoutConfig.payoutVolume(),
                        1.0f
                );
            }
        }

        LOGGER.info("[Payout] Reward Issued.");
    }


    // Hooks for commands
    public static void setReward(ItemStack stack)
    {
        PayoutConfig.setReward(stack);
        PayoutConfig.save();
        LOGGER.info("[Payout] Reward item set to " + stack.getDisplayName());
    }

    public static void setInterval(long millis)
    {
        PayoutConfig.setInterval(millis);
        PayoutConfig.save();
        LOGGER.info("[Payout] Reward time set to " + millis + "ms");
    }

    public static void triggerNow(MinecraftServer server)
    {
        rewardAllPlayers(server);
        lastRunTime = System.currentTimeMillis();
        LOGGER.info("[Payout] Reward manually triggered.");
    }

    public static long getLastRunTime()
    {
        return lastRunTime;
    }
}
