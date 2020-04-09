package fr.mattmouss.gates.setup;

import fr.mattmouss.gates.GatesMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = GatesMod.MODID,bus = Mod.EventBusSubscriber.Bus.MOD)

public class ModSound {
    private static final List<SoundEvent> SOUNDS = new ArrayList();

    public static final SoundEvent ANIMATION_GARAGE =  register("gates:block.garage_animation");
    public static final SoundEvent TOLL_GATE_OPENING = register("gates:block.toll_gate_opening");
    public static final SoundEvent TOLL_GATE_CLOSING = register("gates:block.toll_gate_closing");




    private static SoundEvent register(String name) {
        SoundEvent event = new SoundEvent(new ResourceLocation(name));
        event.setRegistryName(name);
        System.out.println("-----------------Block "+name+" registered !------------------");
        System.out.println("------------------ Name : "+event.getName()+"-------------");
        SOUNDS.add(event);
        return event;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        System.out.println("registering sound ...");
        SOUNDS.forEach((soundEvent) -> {
            event.getRegistry().register(soundEvent);
        });
        SOUNDS.clear();
    }


}
