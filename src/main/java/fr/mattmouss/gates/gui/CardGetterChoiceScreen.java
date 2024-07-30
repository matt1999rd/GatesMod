package fr.mattmouss.gates.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketGiveCard;
import fr.mattmouss.gates.network.PacketLowerPrice;
import fr.mattmouss.gates.network.PacketRaisePrice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class CardGetterChoiceScreen extends AbstractContainerScreen<CardGetterChoiceContainer> {

    private static final int green = 0x007b18;
    private Button plusButton;
    private Button minusButton;
    private static final int white = 0xffffff;

    private final ResourceLocation GUI = new ResourceLocation(GatesMod.MOD_ID, "textures/gui/cg_tech_gui.png");

    public CardGetterChoiceScreen(CardGetterChoiceContainer p_i51105_1_, Inventory p_i51105_2_,Component title) {
        super(p_i51105_1_, p_i51105_2_, Component.nullToEmpty("Choose your fee"));
        this.titleLabelX = 27;
        this.titleLabelY = 4;
    }

    @Override
    public void init() {
        this.imageWidth = 197;
        this.imageHeight = 98;
        super.init();
        plusButton = new Button(leftPos+62,topPos+16,21,20,Component.nullToEmpty("+"),button -> raisePrice());
        minusButton = new Button(leftPos+62,topPos+36,21,20,Component.nullToEmpty("-"), button -> lowerPrice());
        Button doneButton = new Button(leftPos+39, topPos+67, 66, 20, Component.nullToEmpty("Done"), button -> onClose());
        Button getCardButton = new Button(leftPos+116,topPos+67,66,20,Component.nullToEmpty("Get Card"),button -> giveCardToPlayer());
        addRenderableWidget(plusButton);
        addRenderableWidget(minusButton);
        addRenderableWidget(doneButton);
        addRenderableWidget(getCardButton);
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
        drawString(stack,font," "+menu.getId(),114,46,white);
        drawString(stack,font," "+price,shift,56, green );
        drawString(stack,font,"Choose your fee",27,4,white);
    }

    private void raisePrice(){
        this.getMenu().raisePrice();
        System.out.println("raising price..");
        Networking.INSTANCE.sendToServer(new PacketRaisePrice(menu.getTileEntity().getBlockPos()));
    }

    private void lowerPrice(){
        this.getMenu().lowerPrice();
        System.out.println("raising price..");
        Networking.INSTANCE.sendToServer(new PacketLowerPrice(menu.getTileEntity().getBlockPos()));
    }

    private void giveCardToPlayer(){
        int id = this.getMenu().getId();
        Networking.INSTANCE.sendToServer(new PacketGiveCard(id));
    }


    @Override
    protected void renderBg(PoseStack stack,float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0,GUI);
        this.blit(stack,leftPos,topPos,0,0,this.imageWidth, this.imageHeight);
    }
}
