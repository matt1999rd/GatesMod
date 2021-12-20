package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.tileentity.RedstoneTollGateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        assert state != null;
        return state.setValue(BlockStateProperties.POWERED,false);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED) && state.getValue(TG_POSITION)==TollGPosition.CONTROL_UNIT){
            worldIn.setBlock(pos, state.setValue(BlockStateProperties.POWERED, flag), 2);
            TileEntity te=worldIn.getBlockEntity(pos);
            if (te instanceof RedstoneTollGateTileEntity){
                RedstoneTollGateTileEntity rtgte=(RedstoneTollGateTileEntity)te;
                rtgte.startAnimation();
            }
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, World world) {
        TollGPosition tgp = state.getValue(TG_POSITION);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = state.getValue(BlockStateProperties.DOOR_HINGE);
        List<BlockPos> blockPosList = getPositionOfBlockConnected(facing,tgp,dhs,pos);
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
}
