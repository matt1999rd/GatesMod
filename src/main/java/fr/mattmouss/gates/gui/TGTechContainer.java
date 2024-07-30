package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tollcapability.TollStorageCapability;
import fr.mattmouss.gates.tollcapability.ITollStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import java.util.Objects;


public class TGTechContainer extends AbstractContainerMenu {
    private final TollGateTileEntity tileEntity ;
    private final Player playerEntity;
    private final IItemHandler inventory;
    public final int leftCol = 10;
    public final int topRow = 98;


    public TGTechContainer(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player entity) {
        super(ModBlock.TOLLGATE_TECH_CONTAINER, windowId);
        tileEntity = (TollGateTileEntity)world.getBlockEntity(pos);
        playerEntity =entity;
        inventory = new InvWrapper(playerInventory);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getPrice();
            }

            @Override
            public void set(int i) {
                assert tileEntity != null;
                tileEntity.getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(s-> s.setPrice(i));
            }
        });



        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getId();
            }

            @Override
            public void set(int i) {
                assert tileEntity != null;
                tileEntity.getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(s-> s.setId(i));

            }
        });

        assert tileEntity != null;
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h-> addSlot(new SlotItemHandler(h,1,142,18){
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return (stack.getItem() == ModItem.CARD_KEY.asItem());
            }
        }));
        layoutPlayerInventorySlots(topRow);
    }

    public TollGateTileEntity getTileEntity() {
        return tileEntity;
    }


    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(tileEntity.getLevel()),tileEntity.getBlockPos()),
                playerEntity,
                ModBlock.TOLL_GATE);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemStack =ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot !=null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (index ==1) {
                if (!this.moveItemStackTo(stack,2,37,true)) {
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

    public int getId(){
        return tileEntity.getCapability(TollStorageCapability.TOLL_STORAGE).map(ITollStorage::getId).orElse(1);
    }

    public void raisePrice(){
        tileEntity.raisePrice();
    }

    public void lowerPrice(){
        tileEntity.lowerPrice();
    }

    public void changeId(){
        tileEntity.changeId();
    }

    public int getPrice(){
        return tileEntity.getCapability(TollStorageCapability.TOLL_STORAGE).map(ITollStorage::getPrice).orElse(1);
    }

    private void layoutPlayerInventorySlots(int topRow) {//player inventory
        addSlotBox(inventory,9, topRow);
        //hot bar
        topRow+=58;
        addSlotRange(inventory,0, 10,topRow);
    }


    private int addSlotRange(IItemHandler handler, int index, int x, int y) {
        for (int i = 0; i< 9; i++){
            addSlot(new SlotItemHandler(handler, index, x, y));
            x+= 18;
            index++;
        }
        return index;
    }

    private void addSlotBox(IItemHandler handler, int index, int y) {
        for (int j = 0; j< 3; j++) {
            index = addSlotRange(handler,index, 10,y);
            y+= 18;
        }
    }

    public int getKeyId(){
        return tileEntity.getKeyId();
    }

}
