package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.SetIdPacket;
import fr.mattmouss.gates.tileentity.RedstoneTollGateTileEntity;
import fr.mattmouss.gates.tileentity.RedstoneTurnStileTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
        super.fillStateContainer(builder);
    }



    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TurnSPosition position = stateIn.get(TS_POSITION);
        Direction blockFacing = stateIn.get(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing) &&  !(facingState.getBlock() instanceof RedstoneTurnStile)){
            return Blocks.AIR.getDefaultState();
        }
        if (position.isSolid() && facing == Direction.DOWN && !facingState.getMaterial().blocksMovement()){
            return Blocks.AIR.getDefaultState();
        }
        return stateIn;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.get(BlockStateProperties.POWERED) && this.isRightTSB(state)){
            worldIn.setBlockState(pos, state.with(BlockStateProperties.POWERED, flag), 2);
            TileEntity te=worldIn.getTileEntity(pos);
            if (te instanceof RedstoneTurnStileTileEntity){
                RedstoneTurnStileTileEntity rtste=(RedstoneTurnStileTileEntity) te;
                rtste.openTS();
            }
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, World world) {
        TurnSPosition tsp = state.get(TS_POSITION);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = state.get(BlockStateProperties.DOOR_HINGE);
        List<BlockPos> blockPosList = getPositionOfBlockConnected(facing,tsp,dhs,pos);
        if (world.isBlockPowered(pos)){
            return true;
        }
        for (BlockPos neiPos : blockPosList){
            if (world.isBlockPowered(neiPos)){
                return true;
            }
        }
        return false;
    }



    //block facing is the direction of forth block
    private boolean isInnerUpdate(TurnSPosition position, Direction facingUpdate, Direction blockFacing){
        return ( position == RIGHT_BLOCK && facingUpdate == blockFacing.rotateY()) ||
                (position == LEFT_BLOCK  && facingUpdate == blockFacing.rotateYCCW()) ||
                (position == MAIN && (facingUpdate.getAxis() == blockFacing.rotateY().getAxis() || facingUpdate == Direction.UP ) ) ||
                (position == UP_BLOCK && facingUpdate == Direction.DOWN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)return null;
        return state.with(BlockStateProperties.POWERED,false);
    }


}
