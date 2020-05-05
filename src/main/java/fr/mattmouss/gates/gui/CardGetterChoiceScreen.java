package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketLowerPrice;
import fr.mattmouss.gates.network.PacketRaisePrice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class CardGetterChoiceScreen extends ContainerScreen<CardGetterChoiceContainer> {

    private static final int green = 0x007b18;
    private Button plusButton;
    private Button moinsButton;
    private static final int white = 0xffffff;

    private ResourceLocation GUI = new ResourceLocation(GatesMod.MODID, "textures/gui/cg_tech_gui.png");

    public CardGetterChoiceScreen(CardGetterChoiceContainer p_i51105_1_, PlayerInventory p_i51105_2_, ITextComponent p_i51105_3_) {
        super(p_i51105_1_, p_i51105_2_, p_i51105_3_);
    }

    @Override
    public void init() {
        this.xSize = 197;
        this.ySize = 98;
        super.init();
        plusButton = new Button(guiLeft+62,guiTop+16,21,20,"+",button -> raisePrice());
        moinsButton = new Button(guiLeft+62,guiTop+36,21,20,"-",button -> lowerPrice());
        Button doneButton = new Button(guiLeft+39, guiTop+67, 66, 20, "Done", button -> onClose());
        addButton(plusButton);
        addButton(moinsButton);
        addButton(doneButton);
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
        this.drawString(font," "+container.getId(),114,46,white);
        this.drawString(font,"Choose your fee",27,4,white);
        this.drawString(font," "+price,decalage,56, green );
        super.drawGuiContainerForegroundLayer(p_146979_1_,p_146979_2_);
    }

    private void raisePrice(){
        this.getContainer().raisePrice();
        System.out.println("raising price..");
        Networking.INSTANCE.sendToServer(new PacketRaisePrice(container.getTileEntity().getPos()));
    }

    private void lowerPrice(){
        this.getContainer().lowerPrice();
        System.out.println("raising price..");
        Networking.INSTANCE.sendToServer(new PacketLowerPrice(container.getTileEntity().getPos()));
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        this.blit(guiLeft,guiTop,0,0,this.xSize, this.ySize);
    }
}
