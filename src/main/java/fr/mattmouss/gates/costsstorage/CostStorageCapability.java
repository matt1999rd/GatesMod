package fr.mattmouss.gates.costsstorage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.HashMap;

public class CostStorageCapability {
    @CapabilityInject(ICostStorage.class)
    public static Capability<ICostStorage> COST_STORAGE = null;

    public static void register(){
        CapabilityManager.INSTANCE.register(ICostStorage.class, new Capability.IStorage<ICostStorage>() {
            @Override
            public INBT writeNBT(Capability<ICostStorage> capability, ICostStorage instance, Direction side) {
                ListNBT nbt_list = new ListNBT();
                HashMap<Integer,Integer> cost = instance.getCostMap();
                cost.forEach((key,price)->{
                    CompoundNBT tag = new CompoundNBT();
                    tag.putInt("id",key);
                    tag.putInt("price",price);
                    nbt_list.add(tag);
                });
                return nbt_list;
            }

            @Override
            public void readNBT(Capability<ICostStorage> capability, ICostStorage instance, Direction side, INBT nbt) {
                if (nbt instanceof ListNBT){
                    ListNBT nbt_list = (ListNBT)nbt;
                    nbt_list.forEach(intern_nbt -> {
                        CompoundNBT tag = (CompoundNBT)intern_nbt;
                        int id = tag.getInt("id");
                        int price = tag.getInt("price");
                        instance.addIdWithCost(id,price);
                    });
                }
            }
        },CostStorage::new);
    }
}
