package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.GatesMod;

import fr.mattmouss.gates.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;

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
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        int price = container.getRemainingPayment();
        int id = Functions.getIdFromBlockPos(container.getPos());
        this.drawString(Minecraft.getInstance().fontRenderer,"Value to pay :",29,48,white);
        this.drawString(Minecraft.getInstance().fontRenderer," "+price,118,49,white);
        this.drawString(Minecraft.getInstance().fontRenderer,"Toll Gate number "+id,47,6,white);
        super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (container.isGateOpen()){
            this.minecraft.getTextureManager().bindTexture(GUI_OPEN);
        }else {
            this.minecraft.getTextureManager().bindTexture(GUI_CLOSE);
        }
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(relX, relY, 0, 0, this.xSize, this.ySize);
    }

}
