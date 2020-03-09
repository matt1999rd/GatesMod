package fr.mattmouss.gates.doors;

import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class MoreDoor extends DoorBlock {
    public MoreDoor(String key) {
        super(Properties.create(Material.ROCK, MaterialColor.ADOBE)
        .hardnessAndResistance(3.0f)
        .sound(SoundType.METAL).notSolid());
        this.setRegistryName(key);


    }


}
