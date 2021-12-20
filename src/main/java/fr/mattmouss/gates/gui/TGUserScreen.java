package fr.mattmouss.gates.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.mattmouss.gates.GatesMod;

import fr.mattmouss.gates.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class TGUserScreen extends ContainerScreen<TGUserContainer> {

    private final ResourceLocation GUI_OPEN = new ResourceLocation(GatesMod.MOD_ID,"textures/gui/tg_user_gui_open.png");
    private final ResourceLocation GUI_CLOSE = new ResourceLocation(GatesMod.MOD_ID,"textures/gui/tg_user_gui_close.png");
    private static final int white = 0xffffff;
    public TGUserScreen(TGUserContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, ITextComponent.nullToEmpty("Toll Gate number "+container.getId()));
        this.titleLabelX = 30;
        this.titleLabelY = 6;
        this.inventoryLabelX = container.leftCol-1;
        this.inventoryLabelY = container.topRow-1-9;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, partialTicks);
        this.renderTooltip(stack,mouseX,mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack stack,int p_146979_1_, int p_146979_2_) {
        int price = menu.getRemainingPayment();
        int pos_id = Functions.getIdFromBlockPos(menu.getPos());
        FontRenderer font = Minecraft.getInstance().font;
        drawString(stack,font,pos_id+" ",151,52,white);
        drawString(stack,font,"Value to pay :",29,50,white);
        drawString(stack,font," "+price,118,50,white);
        super.renderLabels(stack,p_146979_1_, p_146979_2_);
    }

    @Override
    protected void renderBg(MatrixStack stack,float partialTicks, int mouseX, int mouseY) {
        if (menu.isGateOpen()){
            assert this.minecraft != null;
            this.minecraft.getTextureManager().bind(GUI_OPEN);
        }else {
            assert this.minecraft != null;
            this.minecraft.getTextureManager().bind(GUI_CLOSE);
        }
        this.blit(stack,leftPos, topPos, 0, 0, this.imageWidth+21, this.imageHeight);
    }

}
