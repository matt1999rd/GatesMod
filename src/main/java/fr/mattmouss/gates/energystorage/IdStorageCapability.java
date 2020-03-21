package fr.mattmouss.gates.energystorage;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class IdStorageCapability {
    @CapabilityInject(IIdStorage.class)
    public static Capability<IdStorage> ID_STORAGE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IIdStorage.class, new Capability.IStorage<IIdStorage>()
                {
                    @Override
                    public INBT writeNBT(Capability<IIdStorage> capability, IIdStorage instance, Direction side)
                    {
                        System.out.println("writing IdStorage nbt");
                        return IntNBT.valueOf(instance.getEnergyStored());
                    }

                    public void readNBT(Capability<IIdStorage> capability, IIdStorage instance, Direction side, INBT nbt)
                    {
                        if (instance == null)
                            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                        System.out.println("reading IdStorage nbt");
                        instance.changeId(((IntNBT)nbt).getInt());
                    }
                },
                IdStorage::new);
    }
}
