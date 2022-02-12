package fr.mattmouss.gates.doors;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class MoreDoor extends DoorBlock {
    public MoreDoor(String key) {
        super(AbstractBlock.Properties.of(Material.STONE)
                        .strength(2.0f)
                        .sound(SoundType.METAL)
                        .noOcclusion()
        );
        this.setRegistryName(key);
    }

}
