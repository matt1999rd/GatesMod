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
import net.minecraft.util.BlockRenderLayer;
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
        DoorPlacing placing = state.get(getPlacingBSP());
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        List<BlockPos> blockPosList = getPosOfNeighborBlock(pos,placing,facing);
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

    protected abstract List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing);

    protected abstract EnumProperty<DoorPlacing> getPlacingBSP();

    protected abstract boolean isInternUpdate(DoorPlacing placing,Direction facingUpdate,Direction blockFacing);

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,BlockStateProperties.OPEN,getPlacingBSP(),BlockStateProperties.POWERED);
    }

    //1.14 onBlockActivated
    @Override
    public boolean func_220051_a(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        state = state.cycle(BlockStateProperties.OPEN);
        world.setBlockState(pos,state,10);
        return true;
    }

    //1.15 onBlockActivated
    /*
    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        DoorPlacing placing = state.get(PLACING);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        state = state.cycle(BlockStateProperties.OPEN);
        world.setBlockState(pos,state,10);
        List<BlockPos> blockToChange = getPosOfNeighborBlock(pos,placing,facing);
        for (BlockPos pos1 : blockToChange){
            BlockState state1 = world.getBlockState(pos1);
            state1 = state1.cycle(BlockStateProperties.OPEN);
            world.setBlockState(pos1,state1,10);
        }
        return true;
    }
    */

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState downBlockState = worldIn.getBlockState(pos.down());
        if (state.get(getPlacingBSP()).isUp()){
            Block block = downBlockState.getBlock();
            return (block == this);
        }else {
            return downBlockState.isSolidSide(worldIn,pos.down(),Direction.UP);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        Direction facing = context.getPlacementHorizontalFacing();
        List<BlockPos> neighborFuturePos = getPosOfNeighborBlock(pos,DoorPlacing.LEFT_DOWN,facing);
        neighborFuturePos.add(pos);
        int n=neighborFuturePos.size();
        BlockPos[] positions = new BlockPos[n];
        neighborFuturePos.toArray(positions);
        if (Functions.testReplaceable(context,positions)){
            BlockState state = getDefaultState();
            return state.with(BlockStateProperties.HORIZONTAL_FACING,facing).with(BlockStateProperties.OPEN,false).with(getPlacingBSP(),DoorPlacing.LEFT_DOWN);
        }else {
            return null;
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoorPlacing placing= stateIn.get(getPlacingBSP());
        Direction blockFacing = stateIn.get(BlockStateProperties.HORIZONTAL_FACING);
        if (isInternUpdate(placing,facing,blockFacing)){
            return (facingState.getBlock() == this && facingState.get(getPlacingBSP()) != placing) ?
                    stateIn.with(BlockStateProperties.HORIZONTAL_FACING,facingState.get(BlockStateProperties.HORIZONTAL_FACING))
                            .with(BlockStateProperties.OPEN,facingState.get(BlockStateProperties.OPEN))
                            .with(BlockStateProperties.POWERED,facingState.get(BlockStateProperties.POWERED))
                    : Blocks.AIR.getDefaultState();
        }
        if (!placing.isUp() && facing == Direction.DOWN && !stateIn.isValidPosition(worldIn,currentPos)){
            return Blocks.AIR.getDefaultState();
        }
        return stateIn;
    }

    //1.14.4 function replaced by notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.get(BlockStateProperties.POWERED)){
            worldIn.setBlockState(pos, state.with(BlockStateProperties.POWERED, flag).with(BlockStateProperties.OPEN, flag), 2);
        }
    }

    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack itemstack = player.getHeldItemMainhand();
        if (!world.isRemote && !player.isCreative()) {
            Block.spawnDrops(state, world, pos, null, player, itemstack);
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, Blocks.AIR.getDefaultState(), te, stack);
    }

}
