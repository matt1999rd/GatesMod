package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.GatesMod;

import fr.mattmouss.gates.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;

import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class TGUserScreen extends ContainerScreen<TGUserContainer> {

    private ResourceLocation GUI_OPEN = new ResourceLocation(GatesMod.MODID,"textures/gui/tg_user_gui_open.png");
    private ResourceLocation GUI_CLOSE = new ResourceLocation(GatesMod.MODID,"textures/gui/tg_user_gui_close.png");
    private static final int white = 0xffffff;





    public TGUserScreen(TGUserContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        int price = container.getRemainingPayment();
        int control_id = container.getId();
        int pos_id = Functions.getIdFromBlockPos(container.getPos());
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        this.drawString(font,pos_id+" ",151,52,white);
        this.drawString(font,"Value to pay :",29,50,white);
        this.drawString(font," "+price,118,50,white);
        this.drawString(font,"Toll Gate number "+control_id,30,6,white);
        super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (container.isGateOpen()){
            this.minecraft.getTextureManager().bindTexture(GUI_OPEN);
        }else {
            this.minecraft.getTextureManager().bindTexture(GUI_CLOSE);
        }
        this.blit(guiLeft, guiTop, 0, 0, this.xSize+21, this.ySize);
    }

}
