package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.costsstorage.CostStorage;
import fr.mattmouss.gates.costsstorage.CostStorageCapability;
import fr.mattmouss.gates.costsstorage.ICostStorage;
import fr.mattmouss.gates.energystorage.IdTracker;
import fr.mattmouss.gates.gui.CardGetterChoiceContainer;
import fr.mattmouss.gates.gui.CardGetterContainer;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PutIdsToClientPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CardGetterTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity,IPriceControllingTE {

    //boolean that is set to true when clicking as user
    private static boolean UserGuiOpen = true;

    //boolean that is set to true when value server side has changed
    private static boolean isDirty = true;

    private static boolean idSelectedChange = false;

    //id that is defined by the key we are clicking with
    private static int tech_key_id = -1;

    //id that is set to the id that is selected into the gui
    private static int selected_id = -1;

    private final LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler).cast();

    private final LazyOptional<ICostStorage> costStorage = LazyOptional.of(this::getStorage).cast();

    public ItemStackHandler createHandler() {
        return new ItemStackHandler(2){
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0) {
                    return (stack.getItem() == Items.EMERALD);
                }else{
                    return (stack.getItem() == ModItem.CARD_KEY);
                }

            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if ((stack.getItem() != Items.EMERALD && slot ==0)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    public void setTechKeyId(int keyId){
        tech_key_id = keyId;
    }

    public int getKeyID(){
        return tech_key_id;
    }

    public int getSelectedId(){
        return selected_id;
    }

    public void changeSelectedId(int order){
        costStorage.ifPresent(iCostStorage -> selected_id = (int)iCostStorage.getCostMap().keySet().toArray()[order]);
        idSelectedChange = true;
    }

    public CostStorage getStorage() {
        return new CostStorage();
    }

    public CardGetterTileEntity() {
        super(ModBlock.CARD_GETTER_TILE_TYPE);
    }


    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Card Getter User GUI");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        assert level != null;
        if (UserGuiOpen){
            return new CardGetterContainer(i,level,worldPosition,playerInventory,playerEntity);
        }else {
            return new CardGetterChoiceContainer(i,level,worldPosition,playerEntity);
        }

    }

    public HashMap<Integer,Integer> getIdPriceMap() {
        return costStorage.map(ICostStorage::getCostMap)
                .orElseThrow(()-> new IllegalStateException("Intern Map is not found in the card getter tile entity !"));
    }

    public void addIdAndCost(int new_id,int new_cost){
        costStorage.ifPresent(c->c.addIdWithCost(new_id,new_cost));
    }

    public void removeId(int old_id) {
        costStorage.ifPresent(c->c.removeId(old_id));
    }

    public void changeCost(int id,int new_price){
        costStorage.ifPresent(c-> c.changeCost(id,new_price));
    }

    public void setSide(boolean b) {
        UserGuiOpen = b;
    }

    @Override
    public void tick() {
        assert level != null;
        if (!level.isClientSide){
            List<Integer> id_list = Objects.requireNonNull(level.getServer())
                    .overworld()
                    .getDataStorage()
                    .computeIfAbsent(IdTracker::new,"idgates").getList();
            costStorage.ifPresent(iCostStorage -> {
                id_list.forEach(id->{
                    if (!iCostStorage.containsId(id)){
                        iCostStorage.addIdWithoutCost(id);
                        isDirty = true;
                    }
                });

                HashMap<Integer,Integer> costMap = iCostStorage.getCostMap();

                List<Integer> id_to_remove = new ArrayList<>();
                costMap.forEach((id,price)->{
                    if (!id_list.contains(id)){
                        id_to_remove.add(id);
                        isDirty = true;
                    }
                });
                for (int removed_id : id_to_remove){
                    iCostStorage.removeId(removed_id);
                }
            });
            HashMap<Integer,Integer> costMap = costStorage.map(ICostStorage::getCostMap).orElse(new HashMap<>());
            if (isDirty){
                if (level.players().isEmpty())return;
                for (PlayerEntity serverPlayer : level.players()){
                    Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayerEntity) serverPlayer),new PutIdsToClientPacket(worldPosition,costMap));
                }
            }
            if (UserGuiOpen)manageEmeraldConsumption();
        }
    }

    public void markIdDirty(boolean b){
        isDirty = b;
    }

    private void manageEmeraldConsumption(){
        int price_to_pay = costStorage.map(iCostStorage -> {
            HashMap<Integer,Integer> costMap = iCostStorage.getCostMap();
            if (!costMap.containsKey(selected_id))return -1;
            return costMap.get(selected_id);
        }).orElse(-1);
        if (price_to_pay == -1)return;
        handler.ifPresent(h->{
            ItemStack EmStack = h.getStackInSlot(0);
            int number_of_emerald = EmStack.getCount();
            ItemStack CardStack = h.getStackInSlot(1);
            //if the card is filled with a card we only check the price
            if (!CardStack.isEmpty()){
                //if id change or emerald are removed, and we finally cannot pay the card we remove the card
                boolean flag = idSelectedChange || (number_of_emerald<price_to_pay);
                if (flag) {
                    h.extractItem(1,1,false);
                    idSelectedChange =false;
                }
            }
            if (EmStack.getItem() == Items.EMERALD){
                if (number_of_emerald >= price_to_pay){
                    ItemStack CardKeyStack = new ItemStack(ModItem.CARD_KEY);
                    CardKeyStack.setCount(1);
                    CompoundNBT nbt = CardKeyStack.getOrCreateTag();
                    nbt.putInt("id",selected_id);
                    h.insertItem(1,CardKeyStack,false);
                }
            }

        });
    }

    @Override
    public void load(BlockState state, CompoundNBT compoundNBT) {
        CompoundNBT invTag = compoundNBT.getCompound("inv");
        CompoundNBT costTag = compoundNBT.getCompound("cost");
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        getCapability(CostStorageCapability.COST_STORAGE).ifPresent(c->((INBTSerializable<CompoundNBT>)c).deserializeNBT(costTag));
        super.load(state,compoundNBT);
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compoundNBT);
        });
        getCapability(CostStorageCapability.COST_STORAGE).ifPresent(c->{
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)c).serializeNBT();
            tag.put("cost",compoundNBT);
        });
        return super.save(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return handler.cast();
        }else if (cap == CostStorageCapability.COST_STORAGE){
            return costStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void lowerPrice() {
        costStorage.ifPresent(iCostStorage -> iCostStorage.lowerPrice(tech_key_id));
    }

    @Override
    public void raisePrice() {
        costStorage.ifPresent(iCostStorage -> iCostStorage.raisePrice(tech_key_id));
    }

    @Override
    public int getPrice() {
        AtomicInteger key_id_price= new AtomicInteger(0);
        costStorage.ifPresent(iCostStorage -> key_id_price.set(iCostStorage.getCostMap().get(tech_key_id)));
        return key_id_price.get();
    }

    public void onCardTake() {
        handler.ifPresent(h->{
            int price_to_pay = costStorage.map(iCostStorage -> iCostStorage.getCostMap().get(selected_id)).orElse(-1);
            h.extractItem(0,price_to_pay,false);
        });
    }
}
