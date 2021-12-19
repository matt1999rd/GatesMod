package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;


public abstract class MultDoor extends Block {


    public MultDoor(Properties properties) {
        super(properties);
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, World world) {
        DoorPlacing placing = state.getValue(getPlacingBSP());
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        List<BlockPos> blockPosList = getPosOfNeighborBlock(pos,placing,facing);
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

    protected abstract List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing);

    protected abstract EnumProperty<DoorPlacing> getPlacingBSP();

    protected abstract boolean isInternUpdate(DoorPlacing placing,Direction facingUpdate,Direction blockFacing);

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,BlockStateProperties.OPEN,getPlacingBSP(),BlockStateProperties.POWERED);
    }

    //1.14 onBlockActivated
/*
    @Override
    public boolean func_220051_a(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        state = state.func_177231_a(BlockStateProperties.OPEN);
        world.setBlock(pos,state,10);
        return true;
    }
*/


    //1.15 onBlockActivated

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        state = state.cycle(BlockStateProperties.OPEN);
        world.setBlock(pos,state,10);
        return ActionResultType.SUCCESS;
    }


    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState downBlockState = worldIn.getBlockState(pos.below());
        if (state.getValue(getPlacingBSP()).isUp()){
            Block block = downBlockState.getBlock();
            return (block == this);
        }else {
            return downBlockState.isFaceSturdy(worldIn,pos.below(),Direction.UP);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        List<BlockPos> neighborFuturePos = getPosOfNeighborBlock(pos,DoorPlacing.LEFT_DOWN,facing);
        neighborFuturePos.add(pos);
        int n=neighborFuturePos.size();
        BlockPos[] positions = new BlockPos[n];
        neighborFuturePos.toArray(positions);
        if (Functions.testReplaceable(context,positions)){
            BlockState state = defaultBlockState();
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facing).setValue(BlockStateProperties.OPEN,false).setValue(getPlacingBSP(),DoorPlacing.LEFT_DOWN).setValue(BlockStateProperties.POWERED,false);
        }else {
            return null;
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoorPlacing placing= stateIn.getValue(getPlacingBSP());
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInternUpdate(placing,facing,blockFacing)){
            return (facingState.getBlock() == this && facingState.getValue(getPlacingBSP()) != placing) ?
                    stateIn.setValue(BlockStateProperties.HORIZONTAL_FACING,facingState.getValue(BlockStateProperties.HORIZONTAL_FACING))
                            .setValue(BlockStateProperties.OPEN,facingState.getValue(BlockStateProperties.OPEN))
                            .setValue(BlockStateProperties.POWERED,facingState.getValue(BlockStateProperties.POWERED))
                    : Blocks.AIR.defaultBlockState();
        }
        if (!placing.isUp() && facing == Direction.DOWN && !stateIn.canSurvive(worldIn,currentPos)){
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }

    //1.14.4 function replaced by notSolid()
/*
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
*/

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED)){
            worldIn.setBlock(pos, state.setValue(BlockStateProperties.POWERED, flag).setValue(BlockStateProperties.OPEN, flag), 2);
        }
    }

    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack itemstack = player.getMainHandItem();
        if (!world.isClientSide && !player.isCreative()) {
            Block.dropResources(state, world, pos, null, player, itemstack);
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    public void playerDestroy(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, Blocks.AIR.defaultBlockState(), te, stack);
    }

}
