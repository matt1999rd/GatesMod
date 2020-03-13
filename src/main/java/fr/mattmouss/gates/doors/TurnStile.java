package fr.mattmouss.gates.doors;


import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class TurnStile extends Block {
    public TurnStile() {
        super(Properties.create(Material.BARRIER).hardnessAndResistance(2.0f).sound(SoundType.METAL).notSolid());
        this.setRegistryName("turn_stile");
    }


}
