package fr.mattmouss.gates.setup;

import fr.mattmouss.gates.doors.DoorRegister;
import fr.mattmouss.gates.network.Networking;
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
