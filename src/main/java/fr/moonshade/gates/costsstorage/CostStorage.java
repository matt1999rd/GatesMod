package fr.moonshade.gates.costsstorage;

import fr.moonshade.gates.GatesMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;

public class CostStorage implements ICostStorage, INBTSerializable<CompoundTag> {
    private final HashMap<Integer,Integer> cost;

    public CostStorage(){
        cost = new HashMap<>();
    }


    @Override
    public HashMap<Integer, Integer> getCostMap() {
        return cost;
    }

    @Override
    public void addIdWithoutPrice(int id) {
        addIdWithPrice(id,1);
    }

    @Override
    public void addIdWithPrice(int id, int price) {
        if (!cost.containsKey(id)){
            cost.put(id,price);
        }else {
            GatesMod.logger.warning("Skipping wrong call of function add id where id already exists");
        }
    }

    @Override
    public void removeId(int id) {
        if (!cost.containsKey(id)){
            GatesMod.logger.warning("Skipping wrong call of function remove id without cost where id is not in map");
        }else {
            cost.remove(id);
        }
    }

    @Override
    public boolean containsId(int id) {
        return cost.containsKey(id);
    }

    @Override
    public int getPrice(int id) {
        return cost.get(id);
    }

    @Override
    public void lowerPrice(int id) {
        if (containsId(id)){
            int price = cost.get(id);
            price--;
            cost.replace(id,price);
        }else {
            GatesMod.logger.warning("try to lower price of id that is not register");
        }
    }

    @Override
    public void raisePrice(int id) {
        if (containsId(id)){
            int price = cost.get(id);
            price++;
            cost.replace(id,price);
        }else {
            GatesMod.logger.warning("try to raise price of id that is not register");
        }
    }

    @Override
    public void changeCost(int id, int newCost) {
        if (!cost.containsKey(id)){
            cost.put(id,newCost); // define price to 1 emerald
        }else {
            GatesMod.logger.warning("Skipping wrong call of function change cost where id is not defined");
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt= new CompoundTag();
        ListTag nbt_list = new ListTag();
        cost.forEach((id,price)->{
            CompoundTag id_tag = new CompoundTag();
            id_tag.putInt("id",id);
            id_tag.putInt("price",price);
            nbt_list.add(id_tag);
        });
        nbt.put("id_list",nbt_list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag nbt_list = nbt.getList("id_list",10);
        nbt_list.forEach(intern_nbt -> {
            CompoundTag nbt1 = (CompoundTag)intern_nbt;
            int id=nbt1.getInt("id");
            int price = nbt1.getInt("price");
            cost.put(id,price);
        });
    }
}
