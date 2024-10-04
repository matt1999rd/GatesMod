package fr.moonshade.gates.tscapability;

import net.minecraftforge.common.capabilities.*;

public class TurnStileIdCapability {

    public static Capability<IdTSStorage> TURN_STILE_ID_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    public TurnStileIdCapability(){}

    public static void register(RegisterCapabilitiesEvent event){ event.register(ITSStorage.class);}

}
