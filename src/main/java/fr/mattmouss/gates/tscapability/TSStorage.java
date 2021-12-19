package fr.mattmouss.gates.tscapability;

import fr.mattmouss.gates.energystorage.IdTracker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;

public class TSStorage implements ITSStorage, INBTSerializable<CompoundNBT> {

    private boolean isAnimationInWork;
    private int id;
    private static final Random random = new Random();

    public TSStorage(){
        isAnimationInWork = false;
        id = -1;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void changeId(World world) {
        //trackInt is limited to 16 bit number and there is negative value
        int newId= random.nextInt((int)Math.pow(2,15))+(int)Math.pow(2,15);
        System.out.println("changing id... \n newId = "+newId);
        setId(newId,world);
    }

    @Override
    public void setId(int newId) {
        id = newId;
    }

    @Override
    public void setId(int newId, World world) {
        DimensionSavedDataManager manager = world.getServer().overworld().getDataStorage();
        if (id != -1){
            manager.computeIfAbsent(IdTracker::new,"idgates").removeId(id);
        }
        id = newId;
        manager.computeIfAbsent(IdTracker::new,"idgates").addNewId(newId);
    }

    @Override
    public boolean getAnimationInWork() {
        return isAnimationInWork;
    }

    @Override
    public void startAnimation() {
        isAnimationInWork = true;
    }

    @Override
    public void endAnimation() {
        isAnimationInWork = false;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        System.out.println("------------id written : "+id);
        System.out.println("------------boolean written : "+ isAnimationInWork);
        tag.putBoolean("isopen", isAnimationInWork);
        tag.putInt("id",id);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (isValidNbt(nbt)) {
            System.out.println("------------boolean read : "+nbt.getInt("isopen"));
            System.out.println("------------id read : "+nbt.getInt("id"));
            isAnimationInWork = nbt.getBoolean("isopen");
            id= nbt.getInt("id");
            return;
        }
        System.out.println("nothing has been found");
    }

    private boolean isValidNbt(CompoundNBT nbt) {
        return nbt.contains("isopen") && nbt.contains("id");
    }
}
