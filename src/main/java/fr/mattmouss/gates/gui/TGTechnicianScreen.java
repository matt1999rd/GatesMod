package fr.mattmouss.gates.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.network.ChangeIdPacket;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketLowerPrice;
import fr.mattmouss.gates.network.PacketRaisePrice;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;


public class TGTechnicianScreen extends AbstractContainerScreen<TGTechContainer> {

    private static final int green = 0x007b18;
    private Button plusButton;
    private Button minusButton;
    private static final int white = 0xffffff;

    private final ResourceLocation GUI = new ResourceLocation(GatesMod.MOD_ID, "textures/gui/tg_tech_gui.png");

    public TGTechnicianScreen(TGTechContainer container, Inventory inventory,Component title) {
        super(container,inventory,Component.nullToEmpty("Choose your fee"));
        this.titleLabelX = 27;
        this.titleLabelY = 4;
        this.inventoryLabelX = container.leftCol-1;
        this.inventoryLabelY = container.topRow-1-9;
    }

    @Override
    public void init() {
        super.init();
        plusButton = new Button(leftPos+62,topPos+16,21,20,Component.nullToEmpty("+"),button -> raisePrice());
        minusButton = new Button(leftPos+62,topPos+36,21,20,Component.nullToEmpty("-"), button -> lowerPrice());
        Button changeIdButton = new Button(leftPos+113,  topPos+67, 66, 20, Component.nullToEmpty("change Id"), button -> changeId());
        Button doneButton = new Button(leftPos+39, topPos+67, 66, 20, Component.nullToEmpty("Done"), button -> onClose());
        addRenderableWidget(plusButton);
        addRenderableWidget(minusButton);
        addRenderableWidget(doneButton);
        addRenderableWidget(changeIdButton);
    }

    private void lowerPrice() {
        System.out.println("lowering price..");
        Networking.INSTANCE.sendToServer(new PacketLowerPrice(menu.getTileEntity().getBlockPos()));
        menu.lowerPrice();
    }

    private void raisePrice() {
        System.out.println("raising price..");
        Networking.INSTANCE.sendToServer(new PacketRaisePrice(menu.getTileEntity().getBlockPos()));
        menu.raisePrice();
    }

    private void changeId(){
        System.out.println("changing Id");
        Networking.INSTANCE.sendToServer(new ChangeIdPacket(menu.getTileEntity().getBlockPos(),menu.getKeyId()));
    }


    @Override
    public void render(PoseStack stack,int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, partialTicks);
        this.renderTooltip(stack,mouseX,mouseY);
    }

    @Override
    protected void renderLabels(PoseStack stack,int p_146979_1_, int p_146979_2_) {
        int price = menu.getPrice();
        //price are possible between 1 and 64 emerald
        this.plusButton.active = (price<64);
        this.minusButton.active = (price>1);
        int shift = (price>10)? 7 : 9;
        Font font = Minecraft.getInstance().font;
        drawString(stack,font,"Id : ",95,46,white);
        drawString(stack,font," "+menu.getId(),114,46,white);
        drawString(stack,font," "+price,shift,56, green );
        super.renderLabels(stack,p_146979_1_,p_146979_2_);
    }

    @Override
    protected void renderBg(PoseStack stack,float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0,GUI);
        this.blit(stack,leftPos,topPos,0,0,this.imageWidth+21, this.imageHeight+28);
    }
}
