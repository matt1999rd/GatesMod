package fr.mattmouss.gates.setup;

import fr.mattmouss.gates.doors.DoorRegister;
import fr.mattmouss.gates.network.Networking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModSetup {

    public static ItemGroup itemGroup = new ItemGroup("gate") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(DoorRegister.STONE_DOOR);
        }
    };
    public void init(){
        Networking.registerMessages();
    }
}
