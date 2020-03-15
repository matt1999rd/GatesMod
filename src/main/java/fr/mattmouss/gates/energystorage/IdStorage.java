package fr.mattmouss.gates.energystorage;

import fr.mattmouss.gates.network.ControlIdData;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.energy.EnergyStorage;


public class IdStorage extends EnergyStorage {

    public IdStorage() {
        super(Integer.MAX_VALUE,0,0,0);
    }

    public void changeId(ServerWorld world){
        this.energy = world.getSavedData().getOrCreate(ControlIdData::new,"id_values").getNextId();
    }

}
