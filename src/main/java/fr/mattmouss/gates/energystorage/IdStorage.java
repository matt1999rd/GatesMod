package fr.mattmouss.gates.energystorage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Random;


public class IdStorage extends EnergyStorage implements INBTSerializable<CompoundNBT> {

    private static final Random random = new Random();

    public IdStorage() {
        super(Integer.MAX_VALUE,1,1,-1);
    }

    public void changeId(){
        int newId= random.nextInt((int)Math.pow(2,15))+(int)Math.pow(2,15);
        System.out.println("changing id... \n newId = "+newId);
        setId(newId);
    }

    public void setId(int id){
        System.out.println("new Id set : "+id);
        this.energy= id;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag_id = new CompoundNBT();
        System.out.println("----------------------writing id :"+energy+"-------------");
        tag_id.putInt("id",energy);
        return tag_id;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        System.out.println("--------------------reading id :"+nbt.getInt("id")+"-------------");
        setId(nbt.getInt("id"));
    }
}
