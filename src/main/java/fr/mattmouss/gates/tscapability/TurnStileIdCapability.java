package fr.mattmouss.gates.tscapability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;

public class TurnStileIdCapability {

    public static Capability<IdTSStorage> TURN_STILE_ID_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    public TurnStileIdCapability(){}

    public static void register(RegisterCapabilitiesEvent event){ event.register(ITSStorage.class);}

}
