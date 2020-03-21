package fr.mattmouss.gates.gui;


import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.network.ChangeIdPacket;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketLowerPrice;
import fr.mattmouss.gates.network.PacketRaisePrice;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;


public class TGTechnicianScreen extends ContainerScreen<TGTechContainer> {

    private static final int green = 0x007b18;
    private Button plusButton;
    private Button moinsButton;
    private static final int white = 0xffffff;

    private ResourceLocation GUI = new ResourceLocation(GatesMod.MODID, "textures/gui/tg_tech_gui.png");

    public TGTechnicianScreen(TGTechContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container,inventory,title);
    }

    @Override
    public void init() {
        super.init();
        plusButton = new Button(guiLeft+62,guiTop+16,21,20,"+",button -> raisePrice());
        moinsButton = new Button(guiLeft+62,guiTop+36,21,20,"-",button -> lowerPrice());
        Button changeIdButton = new Button(guiLeft+113,  guiTop+67, 66, 20, "change Id", button -> changeId());
        Button doneButton = new Button(guiLeft+39, guiTop+67, 66, 20, "Done", button -> onClose());
        addButton(plusButton);
        addButton(moinsButton);
        addButton(doneButton);
        addButton(changeIdButton);
    }

    private void lowerPrice() {
        System.out.println("lowering price..");
        Networking.INSTANCE.sendToServer(new PacketLowerPrice(container.getTileEntity().getPos()));
        container.lowerPrice();
    }

    private void raisePrice() {
        System.out.println("raising price..");
        Networking.INSTANCE.sendToServer(new PacketRaisePrice(container.getTileEntity().getPos()));
        container.raisePrice();
    }

    private void changeId(){
        System.out.println("changing Id");
        Networking.INSTANCE.sendToServer(new ChangeIdPacket(container.getTileEntity().getPos(),container.getKeyId()));
    }


    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        int price = container.getPrice();
        //price are possible between 1 and 64 emerald
        this.plusButton.active = (price<64);
        this.moinsButton.active = (price>1);
        int decalage = (price>10)? 7 : 9;
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        this.drawString(font,"Id : ",95,46,white);
        this.drawString(font," "+container.getId(),114,46,white);
        this.drawString(font,"Choose your fee",27,4,white);
        this.drawString(font," "+price,decalage,56, green );
        super.drawGuiContainerForegroundLayer(p_146979_1_,p_146979_2_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        this.blit(guiLeft,guiTop,0,0,this.xSize+21, this.ySize+28);

    }
}
