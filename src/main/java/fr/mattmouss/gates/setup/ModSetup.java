package fr.mattmouss.gates.setup;

import fr.mattmouss.gates.doors.DoorRegister;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.shapes.VoxelShapes;

public class ModSetup {

    public static ItemGroup itemGroup = new ItemGroup("gate") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(DoorRegister.STONE_DOOR);
        }
    };
    public void init(){
        Networking.registerMessages();
    }
}
