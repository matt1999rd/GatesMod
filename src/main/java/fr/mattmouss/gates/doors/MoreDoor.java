package fr.mattmouss.gates.doors;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class MoreDoor extends DoorBlock {
    public MoreDoor(String key) {
        super(BlockBehaviour.Properties.of(Material.STONE)
                        .strength(2.0f)
                        .sound(SoundType.METAL)
                        .noOcclusion()
        );
        this.setRegistryName(key);
    }

}
