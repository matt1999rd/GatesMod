package fr.mattmouss.gates.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketChangeSelectedID;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CardGetterScreen extends ContainerScreen<CardGetterContainer> implements IGuiEventListener {
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
        UpArrow.visible =size>6 && rank_first_element != 0;
        DownArrow.visible=size>6 && rank_first_element != size-6;
        GlStateManager.enableBlend();
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
        if (id_list.size()>=6) {
            //is done when rank first element increase the size of possible value
            // when position of window is at the end of a huge list of id and id are deleted
            while (rank_first_element > id_list.size() - 6) {
                rank_first_element--;
            }
        }
        CardGetterTileEntity cgte = this.getContainer().getTileEntity();

        id_list.forEach((id,price)->{
            if (incr.get()<rank_first_element || incr.get()>rank_first_element+5){
                incr.getAndIncrement();
                return;
            }
            this.minecraft.getTextureManager().bindTexture(WIDGET);
            int offset = (incr.getAndIncrement()-rank_first_element)*23;
            //two first argument are up left coordinate of the button in gui
            //two second argument are up left coordinate of the button in texture WIDGET
            //the two last argument are width and height of button
            int xText = (id == cgte.getSelectedId()) ? 122 : 34; //if it is the right value selected
            this.blit(4,17+offset,xText,0,88,23);
            this.drawString(fontRenderer,id+" ",19,28+offset,white);
            this.drawString(fontRenderer,price+" ",77,28+offset,white);
        });
        super.drawGuiContainerForegroundLayer(p_146979_1_,p_146979_2_);
    }



    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI);
        int WIDTH = (this.width - this.xSize) / 2;
        int HEIGHT = (this.height - this.ySize) / 2;
        //1.14 : .blitOffset 1.15 : getBlitOffset()
        blit(WIDTH, HEIGHT,this.getBlitOffset() , 0.0F, 0.0F, this.xSize, this.ySize, 256, 512);

    }

    private int getIdClicked(double mouseX,double mouseY){
        if (mouseX>guiLeft+91 || mouseX<guiLeft+4 || mouseY<guiTop+17 || mouseY>guiTop+154) return -1;
        else {
            int image_order = MathHelper.floor((mouseY-guiTop-17f)/23f);
            return image_order+rank_first_element;
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        //we do something if we clicked a button with left input of mouse
        int idClicked = getIdClicked(mouseX,mouseY);

        if (idClicked != -1 && button == 0){
            CardGetterTileEntity cgte = this.getContainer().getTileEntity();
            cgte.changeSelectedId(idClicked);
            Networking.INSTANCE.sendToServer(new PacketChangeSelectedID(cgte.getPos(),idClicked));
        }

        return super.mouseClicked(mouseX,mouseY,button);
    }



}
