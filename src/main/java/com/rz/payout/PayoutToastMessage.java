package com.rz.payout;

import com.rz.payout.client.PayoutClientHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PayoutToastMessage
{
    public final String title;
    public final String description;
    public final ItemStack icon;

    public PayoutToastMessage(String title, String description, ItemStack icon)
    {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public static void encode(PayoutToastMessage msg, FriendlyByteBuf buf)
    {
        buf.writeUtf(msg.title);
        buf.writeUtf(msg.description);
        buf.writeItem(msg.icon);
    }

    public static PayoutToastMessage decode(FriendlyByteBuf buf)
    {
        return new PayoutToastMessage(buf.readUtf(), buf.readUtf(), buf.readItem());
    }

    public static void handle(PayoutToastMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                PayoutClientHandler.showToast(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
