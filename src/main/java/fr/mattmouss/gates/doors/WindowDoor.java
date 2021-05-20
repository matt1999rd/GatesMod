package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.tileentity.WindowDoorTileEntity;
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
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.List;

public class WindowDoor extends Block {

    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position", DoorPlacing.class,placing -> {
        return !placing.isCenterY();
    });
    public static IntegerProperty ANIMATION = IntegerProperty.create("animation",0,4);

    protected static final VoxelShape NORTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SOUTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.makeCuboidShape(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);


    public WindowDoor() {
        super(Properties.create(Material.ROCK, MaterialColor.BLACK));
        this.setRegistryName("window_door");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        if (state.get(PLACING).isSide() || state.get(ANIMATION) != 4){
            switch (facing){
                case SOUTH:
                    return SOUTH_AABB;
                case WEST:
                    return WEST_AABB;
                case NORTH:
                    return NORTH_AABB;
                case EAST:
                    return EAST_AABB;
                default:
                    return VoxelShapes.fullCube();
            }
        }
        return VoxelShapes.empty();
    }

    /*
    //not solid for 1.14
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT;
    }

     */

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new WindowDoorTileEntity();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        BlockPos upPos = pos.up();
        Direction facing = context.getPlacementHorizontalFacing();
        BlockPos rightPos = pos.offset(facing.rotateYCCW());
        BlockPos rightUpPos = pos.offset(facing.rotateYCCW()).up();
        BlockPos leftPos = pos.offset(facing.rotateY());
        BlockPos leftUpPos = pos.offset(facing.rotateY()).up();
        if (
                context.getWorld().getBlockState(upPos).isReplaceable(context) &&
                context.getWorld().getBlockState(rightPos).isReplaceable(context) &&
                context.getWorld().getBlockState(rightUpPos).isReplaceable(context) &&
                context.getWorld().getBlockState(leftPos).isReplaceable(context) &&
                context.getWorld().getBlockState(leftUpPos).isReplaceable(context)
        ){
            return super.getStateForPlacement(context);
        }else {
            return null;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            Direction direction = Functions.getDirectionFromEntity(entity,pos);
            //center block down
            world.setBlockState(pos,
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(PLACING, DoorPlacing.CENTER_DOWN)
                            .with(ANIMATION,0)
            );
            //center block up
            world.setBlockState(pos.up(),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(PLACING, DoorPlacing.CENTER_UP)
                            .with(ANIMATION,0)
            );
            //left block down
            world.setBlockState(pos.offset(direction.rotateY()),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(PLACING, DoorPlacing.LEFT_DOWN)
                            .with(ANIMATION,0)
            );
            //left block up
            world.setBlockState(pos.offset(direction.rotateY()).up(),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(PLACING, DoorPlacing.LEFT_UP)
                            .with(ANIMATION,0)
            );
            //right block down
            world.setBlockState(pos.offset(direction.rotateYCCW()),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(PLACING, DoorPlacing.RIGHT_DOWN)
                            .with(ANIMATION,0)
            );
            //right block up
            world.setBlockState(pos.offset(direction.rotateYCCW()).up(),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(PLACING, DoorPlacing.RIGHT_UP)
                            .with(ANIMATION,0)
            );
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoorPlacing placing = stateIn.get(PLACING);
        Direction blockFacing = stateIn.get(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(placing,facing,blockFacing) &&  !(facingState.getBlock() instanceof WindowDoor)){
            return Blocks.AIR.getDefaultState();
        }
        if (placing.isDown() && facing == Direction.DOWN && !facingState.getMaterial().blocksMovement()){
            return Blocks.AIR.getDefaultState();
        }
        return stateIn;
    }

    private boolean isInnerUpdate(DoorPlacing placing, Direction facing, Direction blockFacing) {
        return (placing.hasRightNeighbor() && facing == blockFacing.rotateYCCW()) ||
                (placing.hasLeftNeighbor() && facing == blockFacing.rotateY())  ||
                (placing.isUp() && facing == Direction.DOWN) ||
                (!placing.isUp() && facing == Direction.UP);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,PLACING,ANIMATION);
    }

}
