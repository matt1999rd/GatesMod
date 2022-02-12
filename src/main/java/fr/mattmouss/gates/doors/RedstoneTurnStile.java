package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.tileentity.RedstoneTurnStileTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;


public class RedstoneTurnStile extends AbstractTurnStile {

    public RedstoneTurnStile() {
        super();
        this.setRegistryName("redstone_turn_stile");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RedstoneTurnStileTileEntity();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
        super.createBlockStateDefinition(builder);
    }


    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorTSPartPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED)){
            worldIn.setBlock(pos, state.setValue(BlockStateProperties.POWERED, flag).setValue(WAY_IS_ON,flag), 2);
        }
    }

    private boolean isNeighBorTSPartPowered(BlockPos pos, BlockState state, World world) {
        List<BlockPos> blockPosList = getPositionOfBlockConnected(state,pos);
        if (world.hasNeighborSignal(pos)){
            return true;
        }
        for (BlockPos neiPos : blockPosList){
            if (world.hasNeighborSignal(neiPos)){
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)return null;
        return state.setValue(BlockStateProperties.POWERED,false);
    }

    @Override
    protected BlockState getUpdateState(BlockState state, BlockState facingState) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facingState.getValue(BlockStateProperties.HORIZONTAL_FACING))
                .setValue(BlockStateProperties.POWERED,facingState.getValue(BlockStateProperties.POWERED))
                .setValue(WAY_IS_ON,facingState.getValue(WAY_IS_ON));
    }


}
