package fr.moonshade.gates.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.moonshade.gates.GatesMod;
import fr.moonshade.gates.network.ChangeIdPacket;
import fr.moonshade.gates.network.Networking;
import fr.moonshade.gates.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class TSScreen extends AbstractContainerScreen<TSContainer> {

    private final ResourceLocation GUI = new ResourceLocation(GatesMod.MOD_ID,"textures/gui/ts_tech_gui.png");
    private static final int white = 0xffffff;

    public TSScreen(TSContainer container, Inventory inventory,Component title) {
        super(container, inventory, Component.nullToEmpty("Turn Stile Technician Screen"));
        this.titleLabelX = 11;
        this.titleLabelY = 6;
        this.inventoryLabelX = container.leftCol-1;
        this.inventoryLabelY = container.topRow-1-9;
    }

    @Override
    protected void init() {
        super.init();
        Button changeIdButton = new Button(leftPos+98,  topPos+44, 66, 20, Component.nullToEmpty("change Id"), button -> changeId());
        Button doneButton = new Button(leftPos+11, topPos+44, 66, 20, Component.nullToEmpty("Done"), button -> onClose());
        addRenderableWidget(doneButton);
        addRenderableWidget(changeIdButton);
    }

    @Override
    public void render(PoseStack stack,int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, partialTicks);
        this.renderTooltip(stack,mouseX,mouseY);
    }

    private void changeId(){
        System.out.println("changing Id");
        Networking.INSTANCE.sendToServer(new ChangeIdPacket(menu.getTileEntity().getBlockPos(),menu.getKeyId()));
    }

    @Override
    protected void renderLabels(PoseStack stack,int p_146979_1_, int p_146979_2_) {
        int pos_id = Functions.getIdFromBlockPos(menu.getPos());
        int id = menu.getId();
        Font font = Minecraft.getInstance().font;
        drawString(stack,font,pos_id+" ",33,27,white);
        drawString(stack,font,id+" ",117,27,white);
        super.renderLabels(stack,p_146979_1_,p_146979_2_);
    }

    @Override
    protected void renderBg(PoseStack stack,float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0,GUI);
        this.blit(stack,leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
