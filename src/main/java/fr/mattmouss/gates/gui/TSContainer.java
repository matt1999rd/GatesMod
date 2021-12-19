package fr.mattmouss.gates.gui;


import fr.mattmouss.gates.blocks.ModBlock;
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
    public final int leftCol = 8;
    public final int topRow = 84;

    public TSContainer(int windowId, World world, BlockPos pos, PlayerInventory inventory, PlayerEntity player) {
        super(ModBlock.TURN_STILE_CONTAINER, windowId);
        tileEntity = (TurnStileTileEntity) world.getBlockEntity(pos);
        playerEntity = player;
        this.inventory = new InvWrapper(inventory);

        addDataSlot(new IntReferenceHolder() {
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
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    return (stack.getItem() == ModItem.CARD_KEY.asItem());
                }
            });
        });
        layoutPlayerInventorySlots(leftCol,topRow);
    }


    public BlockPos getPos(){
        return tileEntity.getBlockPos();
    }

    public int getId(){
        return tileEntity.getId();
    }

    public int getKeyId(){
        return tileEntity.getKeyId();
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return stillValid(
                IWorldPosCallable.create(tileEntity.getLevel(),tileEntity.getBlockPos()),
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
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemStack =ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot !=null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (index ==0) {
                if (!this.moveItemStackTo(stack,2,38,true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack,itemStack);
            }else{
                if (stack.getItem() == ModItem.CARD_KEY.asItem()){
                    if (!this.moveItemStackTo(stack,1,2,false)){
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }else if (index < 38 && !this.moveItemStackTo(stack, 2, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn,stack);

        }
        return itemStack;
    }


}
