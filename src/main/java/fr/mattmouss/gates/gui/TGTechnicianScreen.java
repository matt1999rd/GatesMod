package fr.mattmouss.gates.gui;


import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketLowerPrice;
import fr.mattmouss.gates.network.PacketRaisePrice;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;


public class TGTechnicianScreen extends ContainerScreen<TGTechContainer> {

    private static final int WIDTH = 146;
    private static final int HEIGHT = 83;

    private Button plusButton;
    private Button moinsButton;
    private static final int white = 0xffffff;

    private static final int green = 0x007b18;

    private ResourceLocation GUI = new ResourceLocation(GatesMod.MODID, "textures/gui/tg_tech_gui.png");

    private TollGateTileEntity tgte;
    public TGTechnicianScreen(TGTechContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container,inventory,title);
        tgte = container.getTileEntity();
    }

    @Override
    public void init() {
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        plusButton = new Button(relX+62,relY+16,21,18,"+",button -> raisePrice());
        moinsButton = new Button(relX+62,relY+34,21,18,"-",button -> lowerPrice());
        Button doneButton = new Button(relX + 39, relY + 60, 67, 18, "Done", button -> onClose());

        addButton(plusButton);
        addButton(moinsButton);
        addButton(doneButton);
    }

    private void lowerPrice() {
        System.out.println("lowering price..");
        Networking.INSTANCE.sendToServer(new PacketLowerPrice(minecraft.player.dimension,container.getTileEntity().getPos()));
        tgte.lowerPrice();
    }

    private void raisePrice() {
        System.out.println("raising price..");
        Networking.INSTANCE.sendToServer(new PacketRaisePrice(minecraft.player.dimension,container.getTileEntity().getPos()));
        tgte.raisePrice();
    }


    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;
        this.blit(relX, relY, 0, 0, WIDTH, HEIGHT);
        int price = tgte.getPrice();
        //price are possible between 1 and 64 emerald
        this.plusButton.active = (price<64);
        this.moinsButton.active = (price>1);
        int decalage = (price>10)? 7 : 9;
        this.drawString(Minecraft.getInstance().fontRenderer,"Choose your fee",relX+27,relY+4,white);
        this.drawString(Minecraft.getInstance().fontRenderer," "+price,relX+decalage,relY+56, green );
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }
}
