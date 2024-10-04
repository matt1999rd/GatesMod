package fr.moonshade.gates.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.moonshade.gates.GatesMod;

import fr.moonshade.gates.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class TGUserScreen extends AbstractContainerScreen<TGUserContainer> {

    private final ResourceLocation GUI_OPEN = new ResourceLocation(GatesMod.MOD_ID,"textures/gui/tg_user_gui_open.png");
    private final ResourceLocation GUI_CLOSE = new ResourceLocation(GatesMod.MOD_ID,"textures/gui/tg_user_gui_close.png");
    private static final int white = 0xffffff;
    public TGUserScreen(TGUserContainer container, Inventory inventory,Component title) {
        super(container, inventory, Component.nullToEmpty("Toll Gate number "+container.getId()));
        this.titleLabelX = 30;
        this.titleLabelY = 6;
        this.inventoryLabelX = container.leftCol-1;
        this.inventoryLabelY = container.topRow-1-9;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, partialTicks);
        this.renderTooltip(stack,mouseX,mouseY);
    }

    @Override
    protected void renderLabels(PoseStack stack,int p_146979_1_, int p_146979_2_) {
        int price = menu.getRemainingPayment();
        int pos_id = Functions.getIdFromBlockPos(menu.getPos());
        Font font = Minecraft.getInstance().font;
        drawString(stack,font,pos_id+" ",151,52,white);
        drawString(stack,font,"Value to pay :",29,50,white);
        drawString(stack,font," "+price,118,50,white);
        super.renderLabels(stack,p_146979_1_, p_146979_2_);
    }

    @Override
    protected void renderBg(PoseStack stack,float partialTicks, int mouseX, int mouseY) {
        ResourceLocation GUI = (menu.isGateOpen()) ? GUI_OPEN : GUI_CLOSE;
        RenderSystem.setShaderTexture(0,GUI);
        this.blit(stack,leftPos, topPos, 0, 0, this.imageWidth+21, this.imageHeight);
    }

}
