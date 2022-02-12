package fr.mattmouss.gates.tscapability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class TurnStileBooleanCapability {
    @CapabilityInject(TSStorage.class)
    public static Capability<TSStorage> TURN_STILE_BOOLEAN_STORAGE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(TSStorage.class, new Capability.IStorage<TSStorage>() {
            @Override
            public INBT writeNBT(Capability<TSStorage> capability, TSStorage instance, Direction side) {
                CompoundNBT tag = new CompoundNBT();
                tag.putBoolean("isopen",instance.getAnimationInWork());
                return tag;
            }

            @Override
            public void readNBT(Capability<TSStorage> capability, TSStorage instance, Direction side, INBT nbt) {
                if (instance == null) throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT tag = (CompoundNBT)nbt;
                boolean isOpen = tag.getBoolean("isopen");
                if (isOpen){
                    instance.startAnimation();
                }else {
                    instance.endAnimation();
                }
            }
        }, TSStorage::new);
    }

}

