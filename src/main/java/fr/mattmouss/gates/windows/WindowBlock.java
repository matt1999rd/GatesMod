package fr.mattmouss.gates.windows;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;


import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

//TODO : Add the function checkNeighbor to change the block appearance when two window are put together
//TODO : change the sound

public class WindowBlock extends DoorBlock {
    public WindowBlock(String key) {
        super(Properties.create(Material.GLASS)
                .lightValue(0)
                .hardnessAndResistance(3.0f)
                .sound(SoundType.GLASS)
        );
        this.setRegistryName(key);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        Direction lvt_5_1_ = state.get(FACING);
        switch(lvt_5_1_) {
            case EAST:
            default:
                return EAST_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case WEST:
                return WEST_AABB;
            case NORTH:
                return NORTH_AABB;
        }
    }


    public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
        switch(type) {
            case LAND:
            case AIR:
                return state.get(OPEN);
            default:
                return false;
        }
    }

}
