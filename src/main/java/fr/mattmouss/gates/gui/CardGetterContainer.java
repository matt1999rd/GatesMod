package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardGetterContainer extends Container {

    //raison des bug d'affichage :
    // 1. ta mere la pute de canInteractWith : mettre le bon block !!!!
    // 2. erreur null pointer : ajouter un textComponent dans le titre de la gui dans tile entity

    private CardGetterTileEntity tileEntity ;
    private PlayerEntity playerEntity;
    private IItemHandler inventory;


    public CardGetterContainer(int windowId, World world, BlockPos pos, PlayerInventory inventory, PlayerEntity player) {
        super(ModBlock.CARD_GETTER_CONTAINER, windowId);
        tileEntity = (CardGetterTileEntity) world.getTileEntity(pos);
        playerEntity= player;
        this.inventory= new InvWrapper(inventory);
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h->{
            addSlot(new SlotItemHandler(h,0,135,36){
                @Override
                public boolean isItemValid(@Nonnull ItemStack stack) {
                    return stack.getItem()== Items.EMERALD;
                }
            });
            addSlot(new SlotItemHandler(h,1,219,37){
                @Override
                public boolean isItemValid(@Nonnull ItemStack stack) {
                    return false;
                }
            });
        });
        layoutPlayerInventorySlots(107,83);
    }

    public HashMap<Integer,Integer> getIdPriceMap(){
        return tileEntity.getIdPriceMap();
    }



    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(
                IWorldPosCallable.of(tileEntity.getWorld(),tileEntity.getPos()),
                playerEntity,
                ModBlock.CARD_GETTER
        );
    }

    public CardGetterTileEntity getTileEntity(){
        return tileEntity;
    }


    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()){
            ItemStack selecStack = slot.getStack();
            stack = selecStack.copy();
            if (index == 0 || index ==1){//item that are in the two container slot
                if (!mergeItemStack(selecStack,2,38,false)){
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(selecStack,stack);
            }else {
                if (stack.getItem() == Items.EMERALD){//item in inventories that are emerald
                    if (!mergeItemStack(selecStack,0,1,false)){
                        return ItemStack.EMPTY;
                    }
                }else {
                    if (index >1 && index <29){//item in intern inventory that are not emerald
                        if (!mergeItemStack(selecStack,29,38,false)){
                            return ItemStack.EMPTY;
                        }
                    }else {//item in inventory bar that are not emerald
                        if (!mergeItemStack(selecStack,2,29,false)){
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            if (selecStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
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
