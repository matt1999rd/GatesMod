package fr.mattmouss.gates.doors;

import net.minecraft.block.DoorBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
//import net.minecraft.util.BlockRenderLayer;


public class MoreDoor extends DoorBlock {
    public MoreDoor(String key) {
        super(Properties.of(Material.STONE, MaterialColor.COLOR_ORANGE)
                        .strength(2.0f)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        //1.15 function
                        //.notSolid()
        );
        this.setRegistryName(key);
    }


/*
    //1.14.4 function replaced by notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
*/



}
