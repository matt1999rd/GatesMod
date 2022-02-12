package fr.mattmouss.gates.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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

import javax.annotation.Nonnull;
import java.util.List;

public class CardGetterScreen extends ContainerScreen<CardGetterContainer> implements IGuiEventListener {
    public CardGetterScreen(CardGetterContainer container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);
        this.inventoryLabelX = container.leftCol-1;
        this.inventoryLabelY = container.topRow-1-9;
    }
    private static final int white = MathHelper.color(1f,1f,1f);
    private static int rank_first_element = 0;
    private static final int numberOfIdDisplayed = 6;

    private final ResourceLocation WIDGET = new ResourceLocation(GatesMod.MOD_ID,"textures/gui/widget.png");
    private final ResourceLocation GUI = new ResourceLocation(GatesMod.MOD_ID, "textures/gui/card_getter_gui.png");
    private ImageButton UpArrow,DownArrow;


    @Override
    public void init() {
        this.imageWidth = 274;
        super.init();
        //Button GetCardButton = new Button();
        UpArrow =
                new ImageButton(
                        leftPos+108, //position x
                        topPos+21, //position y
                        11, //width of button
                        7, //height of button
                        12, //position x on texture
                        0, //position y on texture
                        7, //difference in y between hovered and non hovered button
                        WIDGET, //png where the image is
                        button -> selectUpId()); //to move to up id
        DownArrow =
                new ImageButton(
                        leftPos+108, //position x
                        topPos+36, //position y
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
    public void render(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack,mouseX, mouseY, partialTicks);
        this.renderTooltip(stack,mouseX,mouseY);
        int size = getMenu().getIdNumber();
        //we can go up and down on list if we are with a too much important list of id
        //we then cannot go up if the first element is the first element of hashMap
        //we then cannot go down if the last element of list is th last one of hashMap
        UpArrow.visible =size>numberOfIdDisplayed && rank_first_element != 0;
        DownArrow.visible=size>numberOfIdDisplayed && rank_first_element != size-numberOfIdDisplayed;
        GlStateManager._enableBlend();
    }



    private void selectDownId() {
        rank_first_element++;
    }

    private void selectUpId() {
        rank_first_element--;
    }

    @Override
    protected void renderLabels(@Nonnull MatrixStack stack, int p_146979_1_, int p_146979_2_) {
        assert this.minecraft != null;
        FontRenderer fontRenderer = this.minecraft.font;
        CardGetterTileEntity cgte = this.getMenu().getTileEntity();
        int nbOfId = getMenu().getIdNumber();
        // is done when rank first element increase the size of possible value
        // when position of window is at the end of a huge list of id and id are deleted
        // if the rank is not optimal (no display of id in last slot), we reset it to a value that uses the whole slots (or 0 if not enough id)
        if (rank_first_element + numberOfIdDisplayed> nbOfId){
            rank_first_element = Math.max(0,nbOfId - numberOfIdDisplayed);
        }

        for (int i=rank_first_element;i<rank_first_element+numberOfIdDisplayed;i++){
            int id = getIdFromOrder(i);
            if (id != -1){
                int price = cgte.getPrice(id);
                this.minecraft.getTextureManager().bind(WIDGET);
                int offset = (i-rank_first_element)*23;
                //two first argument are up left coordinate of the button in gui
                //two second argument are up left coordinate of the button in texture WIDGET
                //the two last argument are width and height of button
                int xText = (id == cgte.getSelectedId()) ? 122 : 34; //if it is the right value selected
                this.blit(stack,4,17+offset,xText,0,88,23);
                drawString(stack,fontRenderer,id+" ",19,28+offset,white);
                drawString(stack,fontRenderer,price+" ",77,28+offset,white);
            }
            //with above if loop security, this only occurs when the rank of the first element is 0 and the number of ID is less than the number of slots
        }
        super.renderLabels(stack,p_146979_1_,p_146979_2_);
    }



    @Override
    protected void renderBg(@Nonnull MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(GUI);
        int WIDTH = (this.width - this.imageWidth) / 2;
        int HEIGHT = (this.height - this.imageHeight) / 2;
        //1.14 : .blitOffset 1.15 : getBlitOffset()
        blit(stack,WIDTH, HEIGHT,this.getBlitOffset() , 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 512);

    }

    private int getIdClicked(double mouseX,double mouseY){
        if (mouseX>leftPos+91 || mouseX<leftPos+4 || mouseY<topPos+17 || mouseY>topPos+154) return -1;
        else {
            int image_order = MathHelper.floor((mouseY-topPos-17f)/23f);
            return getIdFromOrder(image_order+rank_first_element);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //we do something if we clicked a button with left or right input of mouse
        // the id clicked is the area of the mouse if the mouse button is left (0)
        // and is the default non clicked id (-1)    if the mouse button is right(1)
        // nothing is done                         if the mouse button is roll (2)
        int idClicked = (button == 1) ? -1 : getIdClicked(mouseX,mouseY);
        CardGetterTileEntity cgte = this.getMenu().getTileEntity();
        if ((idClicked != -1 && button == 0) || button == 1){
            cgte.changeSelectedId(idClicked);
            Networking.INSTANCE.sendToServer(new PacketChangeSelectedID(cgte.getBlockPos(),idClicked));
        }

        return super.mouseClicked(mouseX,mouseY,button);
    }

    private int getIdFromOrder(int order){
        CardGetterTileEntity cgte = getMenu().getTileEntity();
        List<Integer> idList = cgte.getIdList();
        if (order<0 || order >= idList.size()){
            return -1;
        }else {
            return idList.get(order);
        }
    }

}
