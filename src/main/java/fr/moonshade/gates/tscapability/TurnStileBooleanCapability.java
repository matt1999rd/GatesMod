package fr.moonshade.gates.tscapability;

import net.minecraftforge.common.capabilities.*;

public class TurnStileBooleanCapability {

    public static Capability<TSStorage> TURN_STILE_BOOLEAN_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    public TurnStileBooleanCapability(){}

    public static void register(RegisterCapabilitiesEvent event){ event.register(TSStorage.class);}

}

