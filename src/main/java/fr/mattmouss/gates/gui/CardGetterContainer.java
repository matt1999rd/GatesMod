package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Vector;

public class CardGetterContainer extends Container {

    //raison des bug d'affichage :
    // 1. ta mere la pute de canInteractWith : mettre le bon block !!!!
    // 2. erreur null pointer : ajouter un textComponent dans le titre de la gui dans tile entity

    private CardGetterTileEntity tileEntity ;
    private PlayerEntity playerEntity;
    private IItemHandler inventory;
    public final int leftCol = 107;
    public final int topRow = 83;


    public CardGetterContainer(int windowId, World world, BlockPos pos, PlayerInventory inventory, PlayerEntity player) {
        super(ModBlock.CARD_GETTER_CONTAINER, windowId);
        tileEntity = (CardGetterTileEntity) world.getBlockEntity(pos);
        playerEntity= player;
        this.inventory= new InvWrapper(inventory);
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h->{
            addSlot(new SlotItemHandler(h,0,135,36){
                @Override
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    return stack.getItem()== Items.EMERALD;
                }
            });
            addSlot(new SlotItemHandler(h,1,219,37){
                @Override
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    return false;
                }
                @Override
                public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
                    //we notify te of card taken
                    tileEntity.onCardTake();
                    return super.onTake(entity,stack);
                }
            });
        });
        layoutPlayerInventorySlots(leftCol,topRow);
    }

    public HashMap<Integer,Integer> getIdPriceMap(){
        return tileEntity.getIdPriceMap();
    }



    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return stillValid(
                IWorldPosCallable.create(tileEntity.getLevel(),tileEntity.getBlockPos()),
                playerEntity,
                ModBlock.CARD_GETTER
        );
    }

    public CardGetterTileEntity getTileEntity(){
        return tileEntity;
    }


    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()){
            ItemStack selecStack = slot.getItem();
            stack = selecStack.copy();
            if (index == 0 || index ==1){//item that are in the two container slot
                if (!moveItemStackTo(selecStack,2,38,false)){
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(selecStack,stack);
            }else {
                if (stack.getItem() == Items.EMERALD){//item in inventories that are emerald
                    if (!moveItemStackTo(selecStack,0,1,false)){
                        return ItemStack.EMPTY;
                    }
                }else {
                    if (index >1 && index <29){//item in intern inventory that are not emerald
                        if (!moveItemStackTo(selecStack,29,38,false)){
                            return ItemStack.EMPTY;
                        }
                    }else {//item in inventory bar that are not emerald
                        if (!moveItemStackTo(selecStack,2,29,false)){
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            if (selecStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (selecStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player,selecStack);
        }

        return stack;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {//player inventory
        addSlotBox(inventory,9,leftCol,topRow,9,18,3,18);
        //hotbar
        topRow+=58;
        addSlotRange(inventory,0,leftCol,topRow,9,18);
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0;i< amount;i++){
            addSlot(new SlotItemHandler(handler, index, x, y));
            x+=dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j=0;j<verAmount;j++) {
            index = addSlotRange(handler,index,x,y,horAmount,dx);
            y+=dy;
        }
        return index;
    }
}
