package fr.mattmouss.gates.gui;


import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tollcapability.ITollStorage;
import fr.mattmouss.gates.tollcapability.TollStorageCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;


public class TGUserContainer extends Container {
    private TollGateTileEntity tileEntity ;
    private PlayerEntity playerEntity;
    private IItemHandler inventory;



    public TGUserContainer(int windowId, World world, BlockPos pos, PlayerInventory inventory, PlayerEntity player) {
        super(ModBlock.TOLLGATE_USER_CONTAINER, windowId);
        tileEntity = (TollGateTileEntity) world.getTileEntity(pos);
        playerEntity= player;
        this.inventory= new InvWrapper(inventory);
        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                return getPrice();
            }

            @Override
            public void set(int i) {
                tileEntity.getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(s-> s.setPrice(i));

            }
        });


        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                return getId();
            }

            @Override
            public void set(int i) {
                tileEntity.getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(s-> s.setId(i));

            }
        });

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h->{
            addSlot(new SlotItemHandler(h,0,64,24){
                @Override
                public boolean isItemValid(@Nonnull ItemStack stack) {
                    return stack.getItem()==Items.EMERALD;
                }
            });
            addSlot(new SlotItemHandler(h,1,149,24){
                @Override
                public boolean isItemValid(@Nonnull ItemStack stack) {
                    return (stack.getItem() == ModItem.CARD_KEY.asItem());
                }
            });
        });
        layoutPlayerInventorySlots(10,70);


    }

    private int getPrice() {
        return tileEntity.getCapability(TollStorageCapability.TOLL_STORAGE).map(ITollStorage::getPrice).orElse(1);
    }

    public int getId(){
        return tileEntity.getId();
    }

    public int getRemainingPayment(){
        return tileEntity.getRemainingPayment();
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemStack =ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot !=null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemStack = stack.copy();
            if (index ==0) {
                if (!this.mergeItemStack(stack,2,38,true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack,itemStack);
            } else if (index == 1) {
                if (!this.mergeItemStack(stack,2,38,true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack,itemStack);
            }else{
                if (stack.getItem() == Items.EMERALD) {
                    if (!this.mergeItemStack(stack, 0, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }else if (stack.getItem() == ModItem.CARD_KEY.asItem()){
                    if (!this.mergeItemStack(stack,1,2,false)){
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.mergeItemStack(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }else if (index < 38 && !this.mergeItemStack(stack, 2, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn,stack);

        }
        return itemStack;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {//player inventory
        addSlotBox(inventory,9,leftCol,topRow,9,18,3,18);
        //hotbar
        topRow+=58;
        addSlotRange(inventory,0,leftCol,topRow,9,18);
    }

    public BlockPos getPos(){
        return tileEntity.getPos();
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



    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(
                IWorldPosCallable.of(tileEntity.getWorld(),tileEntity.getPos()),
                playerEntity,
                ModBlock.TOLL_GATE
        );
    }

    public boolean isGateOpen() {
        return tileEntity.isGateOpen();
    }
}
