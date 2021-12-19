package fr.mattmouss.gates.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
        super(container, inventory, ITextComponent.nullToEmpty("Turn Stile Technician Screen"));
        this.titleLabelX = 11;
        this.titleLabelY = 6;
        this.inventoryLabelX = container.leftCol-1;
        this.inventoryLabelY = container.topRow-1-9;
    }

    @Override
    protected void init() {
        super.init();
        Button changeIdButton = new Button(leftPos+98,  topPos+44, 66, 20, ITextComponent.nullToEmpty("change Id"), button -> changeId());
        Button doneButton = new Button(leftPos+11, topPos+44, 66, 20, ITextComponent.nullToEmpty("Done"), button -> onClose());
        addButton(doneButton);
        addButton(changeIdButton);
    }

    @Override
    public void render(MatrixStack stack,int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, partialTicks);
        this.renderTooltip(stack,mouseX,mouseY);
    }

    private void changeId(){
        System.out.println("changing Id");
        Networking.INSTANCE.sendToServer(new ChangeIdPacket(menu.getTileEntity().getBlockPos(),menu.getKeyId()));
    }

    @Override
    protected void renderLabels(MatrixStack stack,int p_146979_1_, int p_146979_2_) {
        int pos_id = Functions.getIdFromBlockPos(menu.getPos());
        int id = menu.getId();
        FontRenderer font = Minecraft.getInstance().font;
        drawString(stack,font,pos_id+" ",33,27,white);
        drawString(stack,font,id+" ",117,27,white);
        super.renderLabels(stack,p_146979_1_,p_146979_2_);
    }

    @Override
    protected void renderBg(MatrixStack stack,float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bind(GUI);
        this.blit(stack,leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
