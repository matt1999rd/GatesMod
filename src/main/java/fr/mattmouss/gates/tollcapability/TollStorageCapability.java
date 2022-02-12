package fr.mattmouss.gates.tollcapability;


import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;


public class TollStorageCapability {
    @CapabilityInject(ITollStorage.class)
    public static Capability<ITollStorage> TOLL_STORAGE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ITollStorage.class, new Capability.IStorage<ITollStorage>() {
            @Override
            public INBT writeNBT(Capability<ITollStorage> capability, ITollStorage instance, Direction side) {
                CompoundNBT tag = new CompoundNBT();
                tag.putBoolean("open",instance.isOpening());
                tag.putBoolean("close",instance.isClosing());
                tag.putInt("price",instance.getPrice());
                tag.putInt("id",instance.getId());
                return tag;
            }

            @Override
            public void readNBT(Capability<ITollStorage> capability, ITollStorage instance, Direction side, INBT nbt) {
                if (!(instance instanceof TollStorage)) throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT tag = (CompoundNBT)nbt;
                Boolean open = tag.getBoolean("open");
                Boolean close = tag.getBoolean("close");
                int price = tag.getInt("price");
                int id = tag.getInt("id");
                instance.setBoolOpen(open);
                instance.setBoolClose(close);
                instance.setId(id);
                instance.setPrice(price);
            }
        },TollStorage::new);
    }

}
