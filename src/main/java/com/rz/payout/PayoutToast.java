package com.rz.payout;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PayoutToast implements Toast
{
    private final Component title;
    private final Component description;
    private final ItemStack icon;
    private long firstDrawTime;
    private boolean newDisplay = true;

    public PayoutToast(Component title, Component description, ItemStack icon)
    {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    @Override
    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long delta)
    {
        if (newDisplay)
        {
            this.firstDrawTime = delta;
            this.newDisplay = false;
        }

        // Background
        guiGraphics.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());

        // Title
        guiGraphics.drawString(toastComponent.getMinecraft().font, title, 30, 7, 0xFFFFFF, false);

        // Description
        guiGraphics.drawString(toastComponent.getMinecraft().font, description, 30, 18, 0xAAAAAA, false);

        // Item icon
        guiGraphics.renderItem(icon, 8, 8);

        return delta - this.firstDrawTime >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}

