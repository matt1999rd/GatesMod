package fr.mattmouss.gates.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ControlIdData extends WorldSavedData {
    List<Integer> idList = new ArrayList<>();

    public ControlIdData() {
        super("id_values");
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT listNBT = (ListNBT) nbt.get("idlist");
        int max = listNBT.size();
        if (max != 0) {
            for (int i = 0; i < max ;i++) {
                CompoundNBT nbt_in = listNBT.getCompound(i);
                int val = ((CompoundNBT) nbt_in).getInt("id");
                if (!idList.contains(val)){
                    idList.add(val);
                }
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT listNBT  = new ListNBT();
        for (int i:idList){
            CompoundNBT id_tag = new CompoundNBT();
            id_tag.putInt("id",i);
            listNBT.add(id_tag);
        }
        nbt.put("idlist",listNBT);
        return nbt;
    }


    public int getNextId() {
        Random random = new Random();
        int newId;
        //we are searching new Id so we need to chck if the list is containing the id if true this function continue as long as a different id is found
        //NB : there is very poor probability for this loop to operate twice (10^-8 %)
        while (true){
            newId = random.nextInt();
            if (!idList.contains(newId)){
                idList.add(newId);
                return newId;
            }
        }
    }
}
