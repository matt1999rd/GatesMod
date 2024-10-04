package fr.moonshade.gates.doors;


import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.tileentity.RedstoneTurnStileTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;


public class RedstoneTurnStile extends AbstractTurnStile {

    public RedstoneTurnStile() {
        super();
        this.setRegistryName("redstone_turn_stile");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RedstoneTurnStileTileEntity(blockPos,blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
        super.createBlockStateDefinition(builder);
    }


    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorTSPartPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED)){
            worldIn.setBlock(pos, state.setValue(BlockStateProperties.POWERED, flag).setValue(WAY_IS_ON,flag), 2);
        }
    }

    private boolean isNeighBorTSPartPowered(BlockPos pos, BlockState state, Level world) {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (type == ModBlock.REDSTONE_TURNSTILE_TILE_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof RedstoneTurnStileTileEntity) {
                ((RedstoneTurnStileTileEntity) t).tick();
            }
        })) : null;
    }
}
