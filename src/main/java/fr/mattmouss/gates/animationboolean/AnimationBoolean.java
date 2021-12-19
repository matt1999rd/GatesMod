package fr.mattmouss.gates.animationboolean;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;


public class AnimationBoolean implements INBTSerializable<CompoundNBT>,IAnimationBoolean {

    private boolean startOpeningAnimation ;
    private boolean startClosingAnimation ;

    public AnimationBoolean(){
        startOpeningAnimation = false;
        startClosingAnimation = false;
    }

    @Override
    public boolean isClosing() {
        return startClosingAnimation;
    }

    @Override
    public boolean isOpening() {
        return startOpeningAnimation;
    }

    public void setBoolClose(Boolean bool){
        startClosingAnimation = bool;
    }

    public void setBoolOpen(Boolean bool){
        startOpeningAnimation = bool;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT tag_bool = new CompoundNBT();
        tag_bool.putBoolean("open",startOpeningAnimation);
        tag_bool.putBoolean("close",startClosingAnimation);
        tag.put("anim",tag_bool);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("anim")) {
            CompoundNBT tag_bool = (CompoundNBT) nbt.get("anim");

            assert tag_bool != null;
            startOpeningAnimation = tag_bool.getBoolean("open");

            startClosingAnimation = tag_bool.getBoolean("close");
        }

    }
}
