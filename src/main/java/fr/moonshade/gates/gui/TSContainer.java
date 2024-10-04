package fr.moonshade.gates.gui;


import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.items.ModItem;
import fr.moonshade.gates.tileentity.TurnStileTileEntity;
import fr.moonshade.gates.tscapability.TurnStileIdCapability;
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

public class TSContainer extends AbstractContainerMenu {

    private final TurnStileTileEntity tileEntity ;
    private final Player playerEntity;
    private final IItemHandler inventory;
    public final int leftCol = 8;
    public final int topRow = 84;

    public TSContainer(int windowId, Level world, BlockPos pos, Inventory inventory, Player player) {
        super(ModBlock.TURN_STILE_CONTAINER, windowId);
        tileEntity = (TurnStileTileEntity) world.getBlockEntity(pos);
        playerEntity = player;
        this.inventory = new InvWrapper(inventory);

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getId();
            }

            @Override
            public void set(int i) {
                assert tileEntity != null;
                tileEntity.getCapability(TurnStileIdCapability.TURN_STILE_ID_STORAGE).ifPresent(s-> s.setId(i));
            }
        });

        assert tileEntity != null;
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h-> addSlot(new SlotItemHandler(h,0,71,23){
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return (stack.getItem() == ModItem.CARD_KEY.asItem());
            }
        }));
        layoutPlayerInventorySlots(topRow);
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
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(tileEntity.getLevel()),tileEntity.getBlockPos()),
                playerEntity,
                ModBlock.TURN_STILE
        );
    }

    public TurnStileTileEntity getTileEntity() {
        return tileEntity;
    }

    private void layoutPlayerInventorySlots(int topRow) {//player inventory
        addSlotBox(inventory,9, topRow);
        //hot bar
        topRow+=58;
        addSlotRange(inventory,0, 8,topRow);
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
            index = addSlotRange(handler,index, 8,y);
            y+= 18;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
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
