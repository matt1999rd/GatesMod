package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.costsstorage.CostStorage;
import fr.mattmouss.gates.costsstorage.CostStorageCapability;
import fr.mattmouss.gates.costsstorage.ICostStorage;
import fr.mattmouss.gates.energystorage.IdTracker;
import fr.mattmouss.gates.gui.CardGetterContainer;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PutIdsToClientPacket;
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
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CardGetterTileEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity {

    private static boolean UserGuiOpen = true;

    private static boolean isDirty = true;

    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler).cast();

    private LazyOptional<ICostStorage> costStorage = LazyOptional.of(this::getStorage).cast();

    public ItemStackHandler createHandler() {
        return new ItemStackHandler(2){
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0) {
                    return (stack.getItem() == Items.EMERALD);
                }else{
                    return false;
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

    public CostStorage getStorage() {
        return new CostStorage();
    }

    public static List<Integer> id_list;

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
        return new CardGetterContainer(i,world,pos,playerInventory,playerEntity);
    }

    public HashMap<Integer,Integer> getIdPriceMap() {
        HashMap<Integer,Integer> cost = new HashMap<>();
        costStorage.ifPresent(c->{
            HashMap<Integer,Integer> internMap =c.getCostMap();
            internMap.forEach(cost::put);
        });
        return cost;
    }

    public void addId(int new_id){
        costStorage.ifPresent(c-> c.addIdWithoutCost(new_id));
    }

    public void addIdAndCost(int new_id,int new_cost){
        costStorage.ifPresent(c->c.addIdWithCost(new_id,new_cost));
    }

    public void removeId(int old_id) {
        costStorage.ifPresent(c->c.removeId(old_id));
    }

    public void changeCost(int id,int new_price){
        costStorage.ifPresent(c->{
            c.changeCost(id,new_price);
        });
    }

    public void setSide(boolean b) {
        UserGuiOpen = b;
    }

    @Override
    public void tick() {
        if (!world.isRemote){
            List<Integer> id_list = world.getServer()
                    .getWorld(DimensionType.OVERWORLD)
                    .getSavedData()
                    .getOrCreate(IdTracker::new,"idgates").getList();
            costStorage.ifPresent(iCostStorage -> {
                id_list.forEach(id->{
                    if (!iCostStorage.containsId(id)){
                        iCostStorage.addIdWithoutCost(id);
                        isDirty = true;
                    }
                });
                HashMap<Integer,Integer> costMap = iCostStorage.getCostMap();
                costMap.forEach((id,price)->{
                    if (!id_list.contains(id)){
                        iCostStorage.removeId(id);
                        isDirty = true;
                    }
                });
            });
            HashMap<Integer,Integer> costMap = costStorage.map(ICostStorage::getCostMap).orElse(new HashMap<>());
            if (isDirty){
                for (PlayerEntity serverPlayer : world.getPlayers()){
                    Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()->{
                        return (ServerPlayerEntity) serverPlayer;
                    }),new PutIdsToClientPacket(pos,costMap));
                    isDirty=false;
                }

            }


        }
    }

    @Override
    public void read(CompoundNBT compoundNBT) {
        CompoundNBT invTag = compoundNBT.getCompound("inv");
        CompoundNBT costTag = compoundNBT.getCompound("cost");
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        getCapability(CostStorageCapability.COST_STORAGE).ifPresent(c->((INBTSerializable<CompoundNBT>)c).deserializeNBT(costTag));
        super.read(compoundNBT);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compoundNBT);
        });
        getCapability(CostStorageCapability.COST_STORAGE).ifPresent(c->{
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)c).serializeNBT();
            tag.put("cost",compoundNBT);
        });
        return super.write(tag);
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

}
