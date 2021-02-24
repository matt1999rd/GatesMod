package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class LargeDoor extends Block {
    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position",DoorPlacing.class,DoorPlacing::isSide);

    public LargeDoor(String key,Material material) {
        super(Properties.create(material, MaterialColor.BLACK));
        this.setRegistryName(key);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Material material = this.material;
        if (material == Material.WOOD){
            return getCircleShape(state);
        }else {
            return getSquareShape(state);
        }
    }

    private VoxelShape getSquareShape(BlockState state) {
        DoorPlacing placing = state.get(PLACING);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        int index = placing.getMeta()*6+facing.getHorizontalIndex()*2+ ((isOpen)?1:0);
        return VoxelDefinition.largeDoorCircleShape[index];
    }

    private VoxelShape getCircleShape(BlockState state) {
        DoorPlacing placing = state.get(PLACING);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        int index = placing.getMeta()*8+facing.getHorizontalIndex()*2+ ((isOpen)?1:0);
        return VoxelDefinition.largeDoorCircleShape[index];
    }

    //1.14.4 function replaced by notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        BlockPos centerPos = pos.up();
        BlockPos upPos = pos.up(2);
        Direction facing = context.getPlacementHorizontalFacing();
        BlockPos rightPos = pos.offset(facing.rotateYCCW());
        BlockPos rightCenterPos = pos.offset(facing.rotateYCCW()).up();
        BlockPos rightUpPos = pos.offset(facing.rotateYCCW()).up(2);
        if (Functions.testReplaceable(context,pos,centerPos,upPos,rightPos,rightCenterPos,rightUpPos)){
            BlockState state = getDefaultState();
            return state.with(BlockStateProperties.HORIZONTAL_FACING,facing).with(BlockStateProperties.OPEN,false).with(PLACING,DoorPlacing.LEFT_DOWN);
        }else {
            return null;
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        worldIn.setBlockState(pos.up(), state.with(PLACING, DoorPlacing.LEFT_CENTER), 3);
        worldIn.setBlockState(pos.up(2), state.with(PLACING, DoorPlacing.LEFT_UP), 3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()),state.with(PLACING,DoorPlacing.RIGHT_DOWN),3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()).up(),state.with(PLACING,DoorPlacing.RIGHT_CENTER),3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()).up(2),state.with(PLACING,DoorPlacing.RIGHT_UP),3);
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

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,BlockStateProperties.OPEN,PLACING,BlockStateProperties.POWERED);
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
    private List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing){
        List<BlockPos> blockToDestroy = Lists.newArrayList();
        BlockPos offsetPos;
        BlockPos offsetPos2;
        if (placing.isUp()){
            offsetPos = pos.down();
            offsetPos2 = pos.down(2);
            blockToDestroy.add(pos.down());
            blockToDestroy.add(pos.down(2));
        }else if (placing.isCenterY()){
            offsetPos = pos.up();
            offsetPos2 = pos.down();
            blockToDestroy.add(pos.up());
            blockToDestroy.add(pos.down());
        } else {
            offsetPos = pos.up();
            offsetPos2 = pos.up(2);
            blockToDestroy.add(pos.up());
            blockToDestroy.add(pos.up(2));
        }
        if (placing.isLeft()){
            offsetPos = offsetPos.offset(facing.rotateYCCW());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.offset(facing.rotateYCCW());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.offset(facing.rotateYCCW()));
        }else {
            offsetPos = offsetPos.offset(facing.rotateY());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.offset(facing.rotateY());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.offset(facing.rotateY()));
        }
        return blockToDestroy;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        if (blockIn != this && flag != state.get(BlockStateProperties.POWERED)){
            worldIn.setBlockState(pos, state.with(BlockStateProperties.POWERED, flag).with(BlockStateProperties.OPEN, flag), 2);
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos,BlockState state,World world) {
        DoorPlacing placing = state.get(PLACING);
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

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoorPlacing placing= stateIn.get(PLACING);
        Direction blockFacing = stateIn.get(BlockStateProperties.HORIZONTAL_FACING);
        if (isInternUpdate(placing,facing,blockFacing)){
            return (facingState.getBlock() == this && facingState.get(PLACING) != placing) ?
                    stateIn.with(BlockStateProperties.HORIZONTAL_FACING,facingState.get(BlockStateProperties.HORIZONTAL_FACING))
                            .with(BlockStateProperties.OPEN,facingState.get(BlockStateProperties.OPEN))
                            .with(BlockStateProperties.POWERED,facingState.get(BlockStateProperties.POWERED))
                    :Blocks.AIR.getDefaultState();
        }
        if (!placing.isUp() && facing == Direction.DOWN && !stateIn.isValidPosition(worldIn,currentPos)){
            return Blocks.AIR.getDefaultState();
        }
        return stateIn;
    }

    private boolean isInternUpdate(DoorPlacing placing,Direction facingUpdate,Direction blockFacing){
        return ( (placing.isUp()|| placing.isCenterY()) && facingUpdate == Direction.DOWN) ||
                ((!placing.isUp()|| placing.isCenterY()) && facingUpdate == Direction.UP) ||
                (placing.isLeft() && facingUpdate == blockFacing.rotateYCCW()) ||
                (!placing.isLeft() && facingUpdate == blockFacing.rotateY());
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState downBlockState = worldIn.getBlockState(pos.down());
        if (state.get(PLACING).isUp()){
            Block block = downBlockState.getBlock();
            return (block == this);
        }else {
            return downBlockState.isSolidSide(worldIn,pos.down(),Direction.UP);
        }
    }
}
