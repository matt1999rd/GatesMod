package fr.mattmouss.gates.energystorage;

import fr.mattmouss.gates.network.ControlIdData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;


public class IdStorage extends EnergyStorage implements INBTSerializable<CompoundNBT> {
    //TODO : find a way to communicate between server and client

    public IdStorage() {
        super(1000000,0,0,0);
    }

    public void changeId(ServerWorld world){
        this.energy = world.getSavedData().getOrCreate(ControlIdData::new,"id_values").getNextId();
    }

    public void changeId(int id){
        this.energy= id;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag_id = new CompoundNBT();
        //System.out.println("----------------------writing price :"+getEnergyStored()+"-------------");
        tag_id.putInt("id",getEnergyStored());
        return tag_id;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        //System.out.println("--------------------reading price :"+nbt.getInt("price")+"-------------");
        changeId(nbt.getInt("id"));

    }
}
