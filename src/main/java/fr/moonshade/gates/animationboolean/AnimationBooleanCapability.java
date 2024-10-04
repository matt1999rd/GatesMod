package fr.moonshade.gates.animationboolean;

import net.minecraftforge.common.capabilities.*;


public class AnimationBooleanCapability {
    public static Capability<AnimationBoolean> ANIMATION_BOOLEAN_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public AnimationBooleanCapability(){

    }
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(AnimationBoolean.class);
    }

}
