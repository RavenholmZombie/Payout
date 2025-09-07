package com.rz.payout;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemArgument;

public class PayoutCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("payout")
                        .requires(source -> source.hasPermission(2)) // admin-only
                        .then(Commands.literal("setItem")
                                .then(Commands.argument("item", ItemArgument.item(context))
                                        .then(Commands.argument("quantity", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    ItemInput input = ItemArgument.getItem(ctx, "item");
                                                    int qty = IntegerArgumentType.getInteger(ctx, "quantity");

                                                    // Build the ItemStack from the parsed argument
                                                    ItemStack stack = input.createItemStack(qty, false);

                                                    // Save it in your config
                                                    Payout.setReward(stack);
                                                    PayoutConfig.save();

                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("[Payout] Reward item set to " + qty + "x " + stack.getDisplayName().getString()),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("setTime")
                                .then(Commands.argument("unit", StringArgumentType.word())
                                        .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    String unit = StringArgumentType.getString(ctx, "unit");
                                                    int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                                    long millis;
                                                    String humanReadable;

                                                    switch (unit.toLowerCase()) {
                                                        case "h": millis = duration * 60L * 60L * 1000L; humanReadable = duration + " hour(s)"; break;
                                                        case "m": millis = duration * 60L * 1000L; humanReadable = duration + " minute(s)"; break;
                                                        case "d": millis = duration * 24L * 60L * 60L * 1000L; humanReadable = duration + " day(s)"; break;
                                                        default:
                                                            ctx.getSource().sendFailure(Component.literal("[Payout Error] Invalid unit, use h|m|d"));
                                                            return 0;
                                                    }

                                                    Payout.setInterval(millis);

                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("[Payout] Reward interval set to " + humanReadable),
                                                            false
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("debug")
                                .then(Commands.literal("trigger")
                                        .executes(ctx -> {
                                            Payout.triggerNow(ctx.getSource().getServer());
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[Payout] Manually triggered reward distribution."),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("reset")
                                        .executes(ctx -> {
                                            PayoutConfig.resetConfig();
                                            PayoutConfig.SPEC.save();
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[Payout] Config has been reset to defaults."),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("config")
                                        .executes(ctx -> {
                                            net.minecraftforge.fml.config.ModConfig cfg = com.rz.payout.PayoutConfig.SERVER_CONFIG;
                                            String path = (cfg != null) ? cfg.getFullPath().toString() : "<null>";
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "[Payout] Config path: " + path + "\n" +
                                                                    "item=" + com.rz.payout.PayoutConfig.rewardItem().getItem().toString() + " x" +
                                                                    com.rz.payout.PayoutConfig.rewardItem().getCount() + "\n" +
                                                                    "intervalMillis=" + com.rz.payout.PayoutConfig.intervalMillis()
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    if (PayoutConfig.SERVER_CONFIG != null) {
                                        // Save any pending changes
                                        PayoutConfig.save();

                                        // Re-attach current config values (Forge will have reloaded if the file changed)
                                        PayoutConfig.attach(PayoutConfig.SERVER_CONFIG);

                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("[Payout] Reloaded " + PayoutConfig.SERVER_CONFIG.getFileName()),
                                                false
                                        );
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("[Payout Error] No server config is currently attached!"));
                                    }
                                    return 1;
                                })
                        )

                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    ItemStack reward = PayoutConfig.rewardItem();
                                    long interval = PayoutConfig.intervalMillis();
                                    long now = System.currentTimeMillis();
                                    long elapsed = now - Payout.getLastRunTime();
                                    long remaining = Math.max(interval - elapsed, 0);
                                    String soundName = PayoutConfig.payoutSound();
                                    String isSoundEnabled = String.valueOf(PayoutConfig.playPayoutSound());
                                    float soundVolume = PayoutConfig.payoutVolume();
                                    String showToast = String.valueOf(PayoutConfig.showToast());
                                    String toastTitle = PayoutConfig.toastTitle();
                                    String toastDesc = PayoutConfig.toastDescription();

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "[Payout] Status:\n" +
                                                            "  • Reward: " + reward.getCount() + "x " + reward.getDisplayName().getString() + "\n" +
                                                            "  • Interval: " + formatDuration(interval) + "\n" +
                                                            "  • Time until next payout: " + formatDuration(remaining) + "\n" +
                                                            "  • Is payout sound enabled: " + isSoundEnabled + "\n" +
                                                            "  • Payout Sound ID: " + soundName + "\n" +
                                                            "  • Payout Sound Volume: " + soundVolume + "\n" +
                                                            "  • Send Toasts to Clients: " + showToast + "\n" +
                                                            "  • Toast Title: " + toastTitle + "\n" +
                                                            "  • Toast Description: " + toastDesc
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("setPayoutSound")
                                .then(Commands.argument("sound", StringArgumentType.string())
                                        .then(Commands.argument("volume", DoubleArgumentType.doubleArg(0.0, 10.0))
                                                .executes(ctx -> {
                                                    String sound = StringArgumentType.getString(ctx, "sound");
                                                    double volume = DoubleArgumentType.getDouble(ctx, "volume");

                                                    PayoutConfig.setPayoutSound(sound, volume);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("[Payout] Payout sound set to " + sound + " (volume " + volume + ")"),
                                                            false
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("setPlayPayoutSound")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                            PayoutConfig.setPlayPayoutSound(enabled);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[Payout] Play payout sound: " + enabled),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("setShowToasts")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                            PayoutConfig.setShowToast(enabled);
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[Payout] Send Toasts to Clients: " + enabled),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("setToastTitle")
                                .then(Commands.argument("title", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String title = StringArgumentType.getString(ctx, "title");
                                            PayoutConfig.setToastTitle(title);
                                            PayoutConfig.SPEC.save(); // persist to file
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[Payout] Toast Title Set: " + PayoutConfig.toastTitle()),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("setToastDescription")
                                .then(Commands.argument("description", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String desc = StringArgumentType.getString(ctx, "description");
                                            PayoutConfig.setToastDescription(desc);
                                            PayoutConfig.SPEC.save(); // persist to file
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("[Payout] Toast Description Set: " + PayoutConfig.toastDescription()),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )

        );
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }
}
