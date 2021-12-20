package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.tileentity.WindowDoorTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
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

public class WindowDoor extends Block {

    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position", DoorPlacing.class,placing -> !placing.isCenterY());
    public static IntegerProperty ANIMATION = IntegerProperty.create("animation",0,4);

    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);


    public WindowDoor() {
        super(Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).noOcclusion()
                //.notSolid()
        );
        this.setRegistryName("window_door");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (state.getValue(PLACING).isSide() || state.getValue(ANIMATION) != 4){
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
                    return VoxelShapes.block();
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
        BlockPos pos = context.getClickedPos();
        BlockPos upPos = pos.above();
        Direction facing = context.getHorizontalDirection();
        BlockPos rightPos = pos.relative(facing.getCounterClockWise());
        BlockPos rightUpPos = pos.relative(facing.getCounterClockWise()).above();
        BlockPos leftPos = pos.relative(facing.getClockWise());
        BlockPos leftUpPos = pos.relative(facing.getClockWise()).above();
        if (
                context.getLevel().getBlockState(upPos).canBeReplaced(context) &&
                context.getLevel().getBlockState(rightPos).canBeReplaced(context) &&
                context.getLevel().getBlockState(rightUpPos).canBeReplaced(context) &&
                context.getLevel().getBlockState(leftPos).canBeReplaced(context) &&
                context.getLevel().getBlockState(leftUpPos).canBeReplaced(context)
        ){
            return super.getStateForPlacement(context);
        }else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            Direction direction = Functions.getDirectionFromEntity(entity,pos);
            //center block down
            world.setBlockAndUpdate(pos,
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(PLACING, DoorPlacing.CENTER_DOWN)
                            .setValue(ANIMATION,0)
            );
            //center block up
            world.setBlockAndUpdate(pos.above(),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(PLACING, DoorPlacing.CENTER_UP)
                            .setValue(ANIMATION,0)
            );
            //left block down
            world.setBlockAndUpdate(pos.relative(direction.getClockWise()),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(PLACING, DoorPlacing.LEFT_DOWN)
                            .setValue(ANIMATION,0)
            );
            //left block up
            world.setBlockAndUpdate(pos.relative(direction.getClockWise()).above(),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(PLACING, DoorPlacing.LEFT_UP)
                            .setValue(ANIMATION,0)
            );
            //right block down
            world.setBlockAndUpdate(pos.relative(direction.getCounterClockWise()),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(PLACING, DoorPlacing.RIGHT_DOWN)
                            .setValue(ANIMATION,0)
            );
            //right block up
            world.setBlockAndUpdate(pos.relative(direction.getCounterClockWise()).above(),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(PLACING, DoorPlacing.RIGHT_UP)
                            .setValue(ANIMATION,0)
            );
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoorPlacing placing = stateIn.getValue(PLACING);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(placing,facing,blockFacing) &&  !(facingState.getBlock() instanceof WindowDoor)){
            return Blocks.AIR.defaultBlockState();
        }
        if (placing.isDown() && facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }

    private boolean isInnerUpdate(DoorPlacing placing, Direction facing, Direction blockFacing) {
        return (placing.hasRightNeighbor() && facing == blockFacing.getCounterClockWise()) ||
                (placing.hasLeftNeighbor() && facing == blockFacing.getClockWise())  ||
                (placing.isUp() && facing == Direction.DOWN) ||
                (!placing.isUp() && facing == Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,PLACING,ANIMATION);
    }

}
