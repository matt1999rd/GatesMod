package fr.mattmouss.gates.gui;


import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import fr.mattmouss.gates.tscapability.TurnStileCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;

public class TSContainer extends Container {

    private TurnStileTileEntity tileEntity ;
    private PlayerEntity playerEntity;
    private IItemHandler inventory;

    public TSContainer(int windowId, World world, BlockPos pos, PlayerInventory inventory, PlayerEntity player) {
        super(ModBlock.TURN_STILE_CONTAINER, windowId);
        tileEntity = (TurnStileTileEntity) world.getTileEntity(pos);
        playerEntity = player;
        this.inventory = new InvWrapper(inventory);

        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                return getId();
            }

            @Override
            public void set(int i) {
                tileEntity.getCapability(TurnStileCapability.TURN_STILE_STORAGE).ifPresent(s-> {
                    s.setId(i);
                });
            }
        });

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h->{
            addSlot(new SlotItemHandler(h,0,71,23){
                @Override
                public boolean isItemValid(@Nonnull ItemStack stack) {
                    return (stack.getItem() == ModItem.CARD_KEY.asItem());
                }
            });
        });
        layoutPlayerInventorySlots(8,84);
    }


    public BlockPos getPos(){
        return tileEntity.getPos();
    }

    public int getId(){
        return tileEntity.getId();
    }

    public int getKeyId(){
        return tileEntity.getKeyId();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(
                IWorldPosCallable.of(tileEntity.getWorld(),tileEntity.getPos()),
                playerEntity,
                ModBlock.TURN_STILE
        );
    }

    public TurnStileTileEntity getTileEntity() {
        return tileEntity;
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
            }else{
                if (stack.getItem() == ModItem.CARD_KEY.asItem()){
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


}
