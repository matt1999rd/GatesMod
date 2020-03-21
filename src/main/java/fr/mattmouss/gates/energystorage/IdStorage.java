package fr.mattmouss.gates.energystorage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;


public class IdStorage implements INBTSerializable<CompoundNBT>,IIdStorage {

    private static final Random random = new Random();
    protected int energy;

    public IdStorage() {
        energy = 0;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    public void changeId(){
        int newId;
        newId = random.nextInt();
        System.out.println("new Id created : " +newId);
        this.energy = MathHelper.abs(newId);
    }

    public void changeId(int id){
        System.out.println("new Id set : "+id);
        this.energy= id;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag_id = new CompoundNBT();
        System.out.println("----------------------writing id :"+energy+"-------------");
        CompoundNBT tag_int = new CompoundNBT();
        tag_int.putInt("id",energy);
        tag_id.put("id_tag",tag_int);
        return tag_id;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CompoundNBT tag_id = (CompoundNBT) nbt.get("id_tag");
        System.out.println("--------------------reading id :"+tag_id.getInt("id")+"-------------");
        changeId(tag_id.getInt("id"));
    }
}
