package fr.mattmouss.gates.tscapability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;

public class TurnStileBooleanCapability {

    public static Capability<TSStorage> TURN_STILE_BOOLEAN_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    public TurnStileBooleanCapability(){}

    public static void register(RegisterCapabilitiesEvent event){ event.register(TSStorage.class);}

}

