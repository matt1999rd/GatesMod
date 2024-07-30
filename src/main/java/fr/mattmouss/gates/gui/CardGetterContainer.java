package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import javax.annotation.Nonnull;
import java.util.Objects;

public class CardGetterContainer extends AbstractContainerMenu {

    private final CardGetterTileEntity tileEntity ;
    private final Player playerEntity;
    private final IItemHandler inventory;
    public final int leftCol = 107;
    public final int topRow = 83;


    public CardGetterContainer(int windowId, Level world, BlockPos pos, Inventory inventory, Player player) {
        super(ModBlock.CARD_GETTER_CONTAINER, windowId);
        tileEntity = (CardGetterTileEntity) world.getBlockEntity(pos);
        playerEntity= player;
        this.inventory= new InvWrapper(inventory);
        assert tileEntity != null;
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
                public void onTake(Player entity, ItemStack stack) {
                    //we notify te of card taken
                    tileEntity.onCardTake();
                    super.onTake(entity,stack);
                }
            });
        });
        layoutPlayerInventorySlots(topRow);
    }


    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(tileEntity.getLevel()),tileEntity.getBlockPos()),
                playerEntity,
                ModBlock.CARD_GETTER
        );
    }

    public CardGetterTileEntity getTileEntity(){
        return tileEntity;
    }

    public int getIdNumber(){
        return tileEntity.getIdNumber();
    }


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()){
            ItemStack selectedStack = slot.getItem();
            stack = selectedStack.copy();
            if (index == 0 || index ==1){//item that are in the two container slot
                if (!moveItemStackTo(selectedStack,2,38,false)){
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(selectedStack,stack);
            }else {
                if (stack.getItem() == Items.EMERALD){//item in inventories that are emerald
                    if (!moveItemStackTo(selectedStack,0,1,false)){
                        return ItemStack.EMPTY;
                    }
                }else {
                    if (index <29){//item in intern inventory that are not emerald
                        if (!moveItemStackTo(selectedStack,29,38,false)){
                            return ItemStack.EMPTY;
                        }
                    }else {//item in inventory bar that are not emerald
                        if (!moveItemStackTo(selectedStack,2,29,false)){
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            if (selectedStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (selectedStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player,selectedStack);
        }

        return stack;
    }

    private void layoutPlayerInventorySlots(int topRow) {//player inventory
        addSlotBox(inventory,9, topRow);
        //hot bar
        topRow+=58;
        addSlotRange(inventory,0, 107,topRow);
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
            index = addSlotRange(handler,index, 107,y);
            y+= 18;
        }
    }
}
