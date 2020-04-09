package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.network.ChangeIdPacket;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class TSScreen extends ContainerScreen<TSContainer> {

    private ResourceLocation GUI = new ResourceLocation(GatesMod.MODID,"textures/gui/ts_tech_gui.png");
    private static final int white = 0xffffff;

    public TSScreen(TSContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        Button changeIdButton = new Button(guiLeft+98,  guiTop+44, 66, 20, "change Id", button -> changeId());
        Button doneButton = new Button(guiLeft+11, guiTop+44, 66, 20, "Done", button -> onClose());
        addButton(doneButton);
        addButton(changeIdButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
    }

    private void changeId(){
        System.out.println("changing Id");
        Networking.INSTANCE.sendToServer(new ChangeIdPacket(container.getTileEntity().getPos(),container.getKeyId()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        int pos_id = Functions.getIdFromBlockPos(container.getPos());
        int id = container.getId();
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        this.drawString(font,pos_id+" ",33,27,white);
        this.drawString(font,id+" ",117,27,white);
        this.drawString(font,"Turn Stile Technician Screen",11,6,white);
        super.drawGuiContainerForegroundLayer(p_146979_1_,p_146979_2_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        this.blit(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
    }
}
