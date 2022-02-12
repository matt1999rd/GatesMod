package fr.mattmouss.gates.setup;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.GatesMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = GatesMod.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)

public class ModSound {
    private static final List<SoundEvent> SOUNDS = Lists.newArrayList();

    public static final SoundEvent ANIMATION_GARAGE =  register("gates:block.garage_animation");
    public static final SoundEvent TOLL_GATE_OPENING = register("gates:block.toll_gate_opening");
    public static final SoundEvent TOLL_GATE_CLOSING = register("gates:block.toll_gate_closing");
    public static final SoundEvent TURN_STILE_PASS = register("gates:block.turn_stile_pass");




    private static SoundEvent register(String name) {
        SoundEvent event = new SoundEvent(new ResourceLocation(name));
        event.setRegistryName(name);
        System.out.println("-----------------Block "+name+" registered !------------------");
        System.out.println("------------------ Name : "+ event +"-------------");
        SOUNDS.add(event);
        return event;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        System.out.println("registering sound ...");
        SOUNDS.forEach((soundEvent) -> event.getRegistry().register(soundEvent));
        SOUNDS.clear();
    }


}
