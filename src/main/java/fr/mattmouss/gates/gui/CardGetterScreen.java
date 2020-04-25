package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.GatesMod;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CardGetterScreen extends ContainerScreen<CardGetterContainer> {
    public CardGetterScreen(CardGetterContainer container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);
    }
    private static final int white = MathHelper.rgb(1f,1f,1f);
    private static int rank_first_element = 0;

    private ResourceLocation WIDGET = new ResourceLocation(GatesMod.MODID,"textures/gui/widget.png");
    private ResourceLocation GUI = new ResourceLocation(GatesMod.MODID, "textures/gui/card_getter_gui.png");
    private ImageButton UpArrow,DownArrow;


    @Override
    public void init() {
        this.xSize = 274;
        super.init();
        //Button GetCardButton = new Button();
        UpArrow =
                new ImageButton(
                        guiLeft+108, //position x
                        guiTop+21, //position y
                        11, //width of button
                        7, //height of button
                        12, //position x on texture
                        0, //position y on texture
                        7, //difference in y between hovered and non hovered button
                        WIDGET, //png where the image is
                        button -> selectUpId()); //to move to up id
        DownArrow =
                new ImageButton(
                        guiLeft+108, //position x
                        guiTop+36, //position y
                        11, //width of button : 11
                        7, //height of button : 7
                        23, //position x on texture
                        0, //position y on texture
                        7, //difference in y between hovered and non hovered button
                        WIDGET, //png where the image is
                        button -> selectDownId()); //to move to up id
        addButton(UpArrow);
        addButton(DownArrow);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
        HashMap<Integer,Integer> id_list= this.getContainer().getIdPriceMap();
        int size = id_list.size();
        //we can go up and down on list if we are with a too much important list of id
        //we then cannot go up if the first element is the first element of hashMap
        //we then cannot go down if the last element of list is th last one of hashMap
        UpArrow.visible =size>7 && rank_first_element != 0;
        DownArrow.visible=size>7 && rank_first_element != size-7;
    }



    @Override
    public boolean isPauseScreen() {
        return true;
    }


    private void selectDownId() {
        rank_first_element++;
    }

    private void selectUpId() {
        rank_first_element--;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        FontRenderer fontRenderer = this.minecraft.fontRenderer;
        HashMap<Integer,Integer> id_list= this.getContainer().getIdPriceMap();
        AtomicInteger incr = new AtomicInteger(0);
        id_list.forEach((id,price)->{
            if (incr.get()<rank_first_element || incr.get()>rank_first_element+6){
                return;
            }
            this.drawString(fontRenderer,"id :"+id+"  price :"+price,6,18+( incr.getAndIncrement() -rank_first_element)*15,white);
        });
        super.drawGuiContainerForegroundLayer(p_146979_1_,p_146979_2_);
    }



    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        int WIDTH = (this.width - this.xSize) / 2;
        int HEIGHT = (this.height - this.ySize) / 2;
        blit(WIDTH, HEIGHT, this.getBlitOffset(), 0.0F, 0.0F, this.xSize, this.ySize, 256, 512);
    }
}
