package fr.mattmouss.gates.animationboolean;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class AnimationBooleanCapability {
    @CapabilityInject(IAnimationBoolean.class)
    public static Capability<IAnimationBoolean> ANIMATION_BOOLEAN_CAPABILITY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IAnimationBoolean.class, new Capability.IStorage<IAnimationBoolean>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IAnimationBoolean> capability, IAnimationBoolean instance, Direction side) {
                CompoundNBT tag = new CompoundNBT();
                tag.putBoolean("open",instance.isOpening());
                tag.putBoolean("close",instance.isClosing());
                return tag;
            }

            @Override
            public void readNBT(Capability<IAnimationBoolean> capability, IAnimationBoolean instance, Direction side, INBT nbt) {
                if (!(instance instanceof AnimationBoolean)) throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT tag = (CompoundNBT)nbt;
                Boolean open_bool = tag.getBoolean("open");
                Boolean close_bool = tag.getBoolean("close");
                instance.setBoolOpen(open_bool);
                instance.setBoolClose(close_bool);

            }
        },AnimationBoolean::new);
    }

}
