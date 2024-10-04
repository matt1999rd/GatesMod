package fr.moonshade.gates.doors;

import fr.moonshade.gates.enum_door.DoorPlacing;
import fr.moonshade.gates.util.Functions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public abstract class MultipleBlockDoor extends Block {


    public MultipleBlockDoor(Properties properties) {
        super(properties);
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, Level world) {
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

    protected abstract boolean isInnerUpdate(DoorPlacing placing, Direction facingUpdate, Direction blockFacing);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,BlockStateProperties.OPEN,getPlacingBSP(),BlockStateProperties.POWERED);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        state = state.cycle(BlockStateProperties.OPEN);
        world.setBlock(pos,state,10);
        return InteractionResult.SUCCESS;
    }


    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoorPlacing placing= stateIn.getValue(getPlacingBSP());
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(placing,facing,blockFacing)){
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

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED)){
            worldIn.setBlock(pos, state.setValue(BlockStateProperties.POWERED, flag).setValue(BlockStateProperties.OPEN, flag), 2);
        }
    }

    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        ItemStack itemstack = player.getMainHandItem();
        if (!world.isClientSide && !player.isCreative()) {
            Block.dropResources(state, world, pos, null, player, itemstack);
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, Blocks.AIR.defaultBlockState(), te, stack);
    }

}
