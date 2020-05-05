package fr.mattmouss.gates.costsstorage;

import fr.mattmouss.gates.GatesMod;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;

public class CostStorage implements ICostStorage, INBTSerializable<CompoundNBT> {
    private HashMap<Integer,Integer> cost;

    public CostStorage(){
        cost = new HashMap<>();
    }


    @Override
    public HashMap<Integer, Integer> getCostMap() {
        return cost;
    }

    @Override
    public void addIdWithoutCost(int id) {
        addIdWithCost(id,1);
    }

    @Override
    public void addIdWithCost(int id,int price) {
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
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt= new CompoundNBT();
        ListNBT nbt_list = new ListNBT();
        cost.forEach((id,price)->{
            CompoundNBT id_tag = new CompoundNBT();
            id_tag.putInt("id",id);
            id_tag.putInt("price",price);
            nbt_list.add(id_tag);
        });
        nbt.put("id_list",nbt_list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ListNBT nbt_list = nbt.getList("id_list",10);
        nbt_list.forEach(inbt -> {
            CompoundNBT nbt1 = (CompoundNBT)inbt;
            int id=nbt1.getInt("id");
            int price = nbt1.getInt("price");
            cost.put(id,price);
        });
    }
}
