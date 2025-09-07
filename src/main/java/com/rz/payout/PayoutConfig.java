package com.rz.payout;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModList;

public class PayoutConfig
{
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<String> ITEM_ID;
    private static final ForgeConfigSpec.IntValue ITEM_COUNT;
    private static final ForgeConfigSpec.LongValue INTERVAL_MILLIS;

    public static ModConfig SERVER_CONFIG;

    static
    {
        BUILDER.push("general");

        ITEM_ID = BUILDER.comment("The item to give players")
                .define("item", "minecraft:diamond");

        ITEM_COUNT = BUILDER.comment("How many items to give")
                .defineInRange("count", 1, 1, 64);

        INTERVAL_MILLIS = BUILDER.comment("Interval in milliseconds between payouts")
                .defineInRange("intervalMillis", 60000L, 1000L, Long.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static ItemStack rewardItem()
    {
        Item item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(ITEM_ID.get()))
                .orElse(Items.DIAMOND);
        return new ItemStack(item, ITEM_COUNT.get());
    }

    public static long intervalMillis()
    {
        return INTERVAL_MILLIS.get();
    }

    public static void setReward(ItemStack stack)
    {
        ITEM_ID.set(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        ITEM_COUNT.set(stack.getCount());
        save();
    }

    public static void setInterval(long millis)
    {
        INTERVAL_MILLIS.set(millis);
        save();
    }

    public static void attach(ModConfig config)
    {
        SERVER_CONFIG = config;
    }

    public static void save()
    {
        if (SERVER_CONFIG != null)
        {
            SERVER_CONFIG.save();
        }
    }
}
