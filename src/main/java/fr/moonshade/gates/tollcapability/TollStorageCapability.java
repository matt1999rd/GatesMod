package fr.moonshade.gates.tollcapability;


import net.minecraftforge.common.capabilities.*;


public class TollStorageCapability {
    public static Capability<ITollStorage> TOLL_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    public TollStorageCapability(){}

    public static void register(RegisterCapabilitiesEvent event){ event.register(ITollStorage.class);}

}
