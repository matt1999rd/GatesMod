package fr.mattmouss.gates.tscapability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class TurnStileIdCapability {
    @CapabilityInject(IdTSStorage.class)
    public static Capability<IdTSStorage> TURN_STILE_ID_STORAGE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IdTSStorage.class, new Capability.IStorage<IdTSStorage>() {
            @Override
            public INBT writeNBT(Capability<IdTSStorage> capability, IdTSStorage instance, Direction side) {
                CompoundNBT tag = new CompoundNBT();
                tag.putInt("id",instance.getId());
                return tag;
            }

            @Override
            public void readNBT(Capability<IdTSStorage> capability, IdTSStorage instance, Direction side, INBT nbt) {
                if (instance == null) throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT tag = (CompoundNBT)nbt;
                int id = tag.getInt("id");
                instance.setId(id);
            }
        }, IdTSStorage::new);
    }
}
