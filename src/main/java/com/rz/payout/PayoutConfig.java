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
    private static final ForgeConfigSpec.ConfigValue<String> PAYOUT_SOUND;
    private static final ForgeConfigSpec.DoubleValue PAYOUT_VOLUME;
    private static final ForgeConfigSpec.BooleanValue PLAY_PAYOUT_SOUND;
    private static final ForgeConfigSpec.BooleanValue SHOW_TOAST;

    // Toast Customizations
    private static final ForgeConfigSpec.ConfigValue<String> TOAST_DESCRIPTION;
    private static final ForgeConfigSpec.ConfigValue<String> TOAST_TITLE;

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

        PAYOUT_SOUND = BUILDER.comment("Sound event ID to play on payout")
                .define("payoutSound", "minecraft:entity.player.levelup");

        PAYOUT_VOLUME = BUILDER.comment("Volume of payout sound")
                .defineInRange("payoutVolume", 1.0, 0.0, 10.0);

        PLAY_PAYOUT_SOUND = BUILDER.comment("Should payout play a sound?")
                .define("playPayoutSound", true);

        SHOW_TOAST = BUILDER.comment("Should the server send a toast packet to connected clients during payout?")
                .define("showToast", true);

        TOAST_TITLE = BUILDER.comment("What should the title of the toast say?")
                .define("toastTitle", "You got paid!");

        TOAST_DESCRIPTION = BUILDER.comment("What should the title of the toast say?")
                .define("toastDescription", "Thanks for playing!");

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

    public static void setToastTitle(String titleIn)
    {
        TOAST_TITLE.set(titleIn);
        save();
    }

    public static void setToastDescription(String descriptionIn)
    {
        TOAST_DESCRIPTION.set(descriptionIn);
        save();
    }

    public static void setShowToast(boolean input)
    {
        SHOW_TOAST.set(input);
        save();
    }

    public static boolean showToast()
    {
        return SHOW_TOAST.get();
    }

    public static String toastTitle()
    {
        return TOAST_TITLE.get();
    }

    public static String toastDescription()
    {
        return TOAST_DESCRIPTION.get();
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

    public static String payoutSound()
    {
        return PAYOUT_SOUND.get();
    }

    public static float payoutVolume()
    {
        return PAYOUT_VOLUME.get().floatValue();
    }

    public static boolean playPayoutSound()
    {
        return PLAY_PAYOUT_SOUND.get();
    }

    public static void setPayoutSound(String sound, double volume)
    {
        PAYOUT_SOUND.set(sound);
        PAYOUT_VOLUME.set(volume);
        save();
    }

    public static void setPlayPayoutSound(boolean enabled)
    {
        PLAY_PAYOUT_SOUND.set(enabled);
        save();
    }

    public static void resetConfig()
    {
        setToastTitle("You got paid!");
        setToastDescription("Thanks for playing!");
        setShowToast(true);
        setInterval(60000);
        setPayoutSound("minecraft:entity.player.levelup", 1.0);
        setReward(new ItemStack(Items.DIAMOND));
        ITEM_COUNT.set(1);
        save();
    }

    public static void save()
    {
        if (SERVER_CONFIG != null)
        {
            SERVER_CONFIG.save();
        }
    }
}
