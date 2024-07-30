package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.tileentity.RedstoneTollGateTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneTollGate extends AbstractTollGate{
    public RedstoneTollGate(){
        super();
        this.setRegistryName("redstone_toll_gate");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RedstoneTollGateTileEntity(blockPos,blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)return null;
        return state.setValue(BlockStateProperties.POWERED,false);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED) && state.getValue(TG_POSITION)==TollGPosition.CONTROL_UNIT){
            worldIn.setBlock(pos, state.setValue(BlockStateProperties.POWERED, flag), 2);
            BlockEntity te=worldIn.getBlockEntity(pos);
            if (te instanceof RedstoneTollGateTileEntity){
                RedstoneTollGateTileEntity rtgte=(RedstoneTollGateTileEntity)te;
                rtgte.startAnimation();
            }
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, Level world) {
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (type == ModBlock.REDSTONE_TOLL_GATE_ENTITY_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof RedstoneTollGateTileEntity) {
                ((RedstoneTollGateTileEntity) t).tick(level1,blockState);
            }
        })) : null;
    }
}
