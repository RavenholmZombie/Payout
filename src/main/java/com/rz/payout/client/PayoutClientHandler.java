package com.rz.payout.client;

import com.rz.payout.PayoutToast;
import com.rz.payout.PayoutToastMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PayoutClientHandler
{
    public static void showToast(PayoutToastMessage msg)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.getToasts().addToast(new PayoutToast(
                Component.literal(msg.title).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                Component.literal(msg.description).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                msg.icon
        ));
    }
}
