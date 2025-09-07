package com.rz.payout;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class PayoutCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("payout")
                        .requires(source -> source.hasPermission(2)) // admin-only
                        .then(Commands.literal("setItem")
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .then(Commands.argument("quantity", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    String itemId = StringArgumentType.getString(ctx, "item");
                                                    int qty = IntegerArgumentType.getInteger(ctx, "quantity");
                                                    Item item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(itemId))
                                                            .orElse(null);
                                                    if (item == null) {
                                                        ctx.getSource().sendFailure(Component.literal("[Payout Error] Unknown item: " + itemId));
                                                        return 0;
                                                    }

                                                    ItemStack stack = new ItemStack(item, qty);
                                                    Payout.setReward(stack);

                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("[Payout] Reward item set to " + qty + "x " + itemId),
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
                        )
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    net.minecraftforge.fml.config.ConfigTracker.INSTANCE.loadConfigs(
                                            net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                                            net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get()
                                    );
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("[Payout] Reloaded giver-server.toml"),
                                            false
                                    );
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

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Giver Status:\n" +
                                                            "  • Reward: " + reward.getCount() + "x " + reward.getDisplayName().getString() + "\n" +
                                                            "  • Interval: " + formatDuration(interval) + "\n" +
                                                            "  • Time until next payout: " + formatDuration(remaining)
                                            ),
                                            false
                                    );
                                    return 1;
                                })
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
