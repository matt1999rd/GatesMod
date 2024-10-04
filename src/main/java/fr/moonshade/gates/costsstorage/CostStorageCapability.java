package fr.moonshade.gates.costsstorage;


import net.minecraftforge.common.capabilities.*;


public class CostStorageCapability {
    public static Capability<CostStorage> COST_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    public CostStorageCapability(){}

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(CostStorage.class);
    }

}
