package fr.mattmouss.gates.tscapability;

import fr.mattmouss.gates.energystorage.IdTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Objects;
import java.util.Random;

public class IdTSStorage implements IIdTSStorage,INBTSerializable<CompoundTag> {

    private int id;
    private static final Random random = new Random();

    public IdTSStorage(){
        id = -1;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void changeId(Level world) {
        //trackInt is limited to 16-bit number and there is negative value
        int newId= random.nextInt((int)Math.pow(2,15))+(int)Math.pow(2,15);
        System.out.println("changing id... \n newId = "+newId);
        setId(newId,world);
    }

    @Override
    public void setId(int newId) {
        id = newId;
    }

    @Override
    public void setId(int newId, Level world) {
        DimensionDataStorage manager = Objects.requireNonNull(world.getServer()).overworld().getDataStorage();
        if (id != -1){
            manager.computeIfAbsent(IdTracker::new,IdTracker::new,"idgates").removeId(id);
        }
        id = newId;
        manager.computeIfAbsent(IdTracker::new,IdTracker::new,"idgates").addNewId(newId);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        System.out.println("------------id written : "+id);
        tag.putInt("id",id);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (isValidNbt(nbt)) {
            System.out.println("------------id read : "+nbt.getInt("id"));
            id= nbt.getInt("id");
            return;
        }
        System.out.println("nothing has been found");
    }

    protected boolean isValidNbt(CompoundTag nbt) {
        return nbt.contains("id");
    }

}
