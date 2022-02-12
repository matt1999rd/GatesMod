package fr.mattmouss.gates.animationboolean;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;


public class AnimationBooleanCapability {
    @CapabilityInject(AnimationBoolean.class)
    public static Capability<AnimationBoolean> ANIMATION_BOOLEAN_CAPABILITY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(AnimationBoolean.class, new Capability.IStorage<AnimationBoolean>() {
            @Override
            public INBT writeNBT(Capability<AnimationBoolean> capability, AnimationBoolean instance, Direction side) {
                CompoundNBT tag = new CompoundNBT();
                tag.putBoolean("open",instance.isOpening());
                tag.putBoolean("close",instance.isClosing());
                return tag;
            }

            @Override
            public void readNBT(Capability<AnimationBoolean> capability, AnimationBoolean instance, Direction side, INBT nbt) {
                if (instance == null) throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT tag = (CompoundNBT)nbt;
                Boolean open_bool = tag.getBoolean("open");
                Boolean close_bool = tag.getBoolean("close");
                instance.setBoolOpen(open_bool);
                instance.setBoolClose(close_bool);

            }
        },AnimationBoolean::new);
    }

}
