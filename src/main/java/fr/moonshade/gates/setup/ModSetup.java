package fr.moonshade.gates.setup;

import fr.moonshade.gates.doors.DoorRegister;
import fr.moonshade.gates.network.Networking;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModSetup {

    public static CreativeModeTab itemGroup = new CreativeModeTab("gate") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(DoorRegister.STONE_DOOR);
        }
    };
    public void init(){
        Networking.registerMessages();
    }
}
