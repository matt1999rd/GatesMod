package fr.mattmouss.gates.tscapability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class TSStorage implements ITSStorage, INBTSerializable<CompoundNBT> {
    private boolean isAnimationInWork;

    public TSStorage(){
        isAnimationInWork = false;
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
        System.out.println("------------boolean written : "+ isAnimationInWork);
        tag.putBoolean("isopen", isAnimationInWork);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (isValidNbt(nbt)) {
            System.out.println("------------boolean read : " + nbt.getInt("isopen"));
            isAnimationInWork = nbt.getBoolean("isopen");
        }
        System.out.println("nothing has been found");
    }

    protected boolean isValidNbt(CompoundNBT nbt){
        return nbt.contains("isopen");
    }
}
