package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.tileentity.RedstoneTurnStileTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static fr.mattmouss.gates.enum_door.TurnSPosition.*;

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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TurnSPosition position = stateIn.getValue(TS_POSITION);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing) &&  !(facingState.getBlock() instanceof RedstoneTurnStile)){
            return Blocks.AIR.defaultBlockState();
        }
        if (position.isSolid() && facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED) && this.isControlUnit(state)){
            worldIn.setBlock(pos, state.setValue(BlockStateProperties.POWERED, flag), 2);
            TileEntity te=worldIn.getBlockEntity(pos);
            if (te instanceof RedstoneTurnStileTileEntity){
                RedstoneTurnStileTileEntity rtste=(RedstoneTurnStileTileEntity) te;
                rtste.openTS();
            }
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, World world) {
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



    //block facing is the direction of forth block
    private boolean isInnerUpdate(TurnSPosition position, Direction facingUpdate, Direction blockFacing){
        return ( position == RIGHT_BLOCK && facingUpdate == blockFacing.getClockWise()) ||
                (position == LEFT_BLOCK  && facingUpdate == blockFacing.getCounterClockWise()) ||
                (position == MAIN && (facingUpdate.getAxis() == blockFacing.getClockWise().getAxis() || facingUpdate == Direction.UP ) ) ||
                (position == UP_BLOCK && facingUpdate == Direction.DOWN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)return null;
        return state.setValue(BlockStateProperties.POWERED,false);
    }


}
