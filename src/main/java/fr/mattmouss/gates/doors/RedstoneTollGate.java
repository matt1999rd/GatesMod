package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.tileentity.RedstoneTollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RedstoneTollGate extends AbstractTollGate{
    public RedstoneTollGate(){
        super();
        this.setRegistryName("redstone_toll_gate");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RedstoneTollGateTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
        super.fillStateContainer(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        Direction facing = context.getPlacementHorizontalFacing();
        PlayerEntity entity = context.getPlayer();
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,facing);
        List<BlockPos> neighborFuturePos = getPositionOfBlockConnected(facing,TollGPosition.CONTROL_UNIT,dhs,pos);
        neighborFuturePos.add(pos);
        int n=neighborFuturePos.size();
        BlockPos[] positions = new BlockPos[n];
        neighborFuturePos.toArray(positions); //todo : test replaceble is not working for redstonetollgate
        if (Functions.testReplaceable(context,positions)){
            BlockState state = getDefaultState();
            return state.with(BlockStateProperties.HORIZONTAL_FACING,facing).with(TG_POSITION,TollGPosition.CONTROL_UNIT).with(BlockStateProperties.POWERED,false).with(ANIMATION,0);
        }else {
            return null;
        }
    }

    public List<BlockPos> getPositionOfBlockConnected(Direction direction,TollGPosition tgp,DoorHingeSide dhs,BlockPos pos) {
        //ajout de tout les blocks
        List<BlockPos> posList = new ArrayList<>();
        Direction extDirection = Functions.getDirectionOfExtBlock(direction,dhs);
        BlockPos emptyBasePos = getEmptyBasePos(tgp,extDirection,direction,pos);
        //block emptybase
        posList.add(emptyBasePos);
        //block de control unit
        posList.add(emptyBasePos.offset(direction));
        //block main et emptyext
        posList.add(emptyBasePos.offset(extDirection));
        posList.add(emptyBasePos.offset(extDirection,2));
        //block up
        posList.add(emptyBasePos.up());
        return posList;
    }

    private BlockPos getEmptyBasePos(TollGPosition tgp, Direction extDirection, Direction facing,BlockPos pos) {
        switch (tgp) {
            case EMPTY_BASE:
                return pos;
            case MAIN:
                return pos.offset(extDirection.getOpposite());
            case EMPTY_EXT:
                return pos.offset(extDirection.getOpposite(), 2);
            case UP_BLOCK:
                return pos.down();
            case CONTROL_UNIT:
                return pos.offset(facing.getOpposite());
            default:
                throw new NullPointerException("TollGatePosition of block at position :" + pos + "has null attribut for tollgateposition");
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.get(BlockStateProperties.POWERED) && state.get(TG_POSITION)==TollGPosition.CONTROL_UNIT){
            worldIn.setBlockState(pos, state.with(BlockStateProperties.POWERED, flag), 2);
            TileEntity te=worldIn.getTileEntity(pos);
            if (te instanceof RedstoneTollGateTileEntity){
                RedstoneTollGateTileEntity rtgte=(RedstoneTollGateTileEntity)te;
                rtgte.startAnimation();
            }
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, World world) {
        TollGPosition tgp = state.get(TG_POSITION);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = state.get(BlockStateProperties.DOOR_HINGE);
        List<BlockPos> blockPosList = getPositionOfBlockConnected(facing,tgp,dhs,pos);
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
}
