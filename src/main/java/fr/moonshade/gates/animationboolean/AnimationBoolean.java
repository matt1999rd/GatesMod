package fr.moonshade.gates.animationboolean;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;


public class AnimationBoolean implements INBTSerializable<CompoundTag>,IAnimationBoolean {

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
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag tag_bool = new CompoundTag();
        tag_bool.putBoolean("open",startOpeningAnimation);
        tag_bool.putBoolean("close",startClosingAnimation);
        tag.put("anim",tag_bool);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("anim")) {
            CompoundTag tag_bool = (CompoundTag) nbt.get("anim");

            assert tag_bool != null;
            startOpeningAnimation = tag_bool.getBoolean("open");

            startClosingAnimation = tag_bool.getBoolean("close");
        }

    }
}
