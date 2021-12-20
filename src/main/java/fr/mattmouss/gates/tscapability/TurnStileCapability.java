package fr.mattmouss.gates.tscapability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class TurnStileCapability {
    @CapabilityInject(ITSStorage.class)
    public static Capability<ITSStorage> TURN_STILE_STORAGE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ITSStorage.class, new Capability.IStorage<ITSStorage>() {
            @Override
            public INBT writeNBT(Capability<ITSStorage> capability, ITSStorage instance, Direction side) {
                CompoundNBT tag = new CompoundNBT();
                tag.putBoolean("isopen",instance.getAnimationInWork());
                tag.putInt("id",instance.getId());
                return tag;
            }

            @Override
            public void readNBT(Capability<ITSStorage> capability, ITSStorage instance, Direction side, INBT nbt) {
                if (!(instance instanceof TSStorage)) throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT tag = (CompoundNBT)nbt;
                boolean isOpen = tag.getBoolean("isopen");
                int id = tag.getInt("id");
                if (isOpen){
                    instance.startAnimation();
                }else {
                    instance.endAnimation();
                }
                instance.setId(id);
            }
        },TSStorage::new);
    }

}

