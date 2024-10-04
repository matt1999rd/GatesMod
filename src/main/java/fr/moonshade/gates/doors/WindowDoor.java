package fr.moonshade.gates.doors;

import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.enum_door.DoorPlacing;
import fr.moonshade.gates.tileentity.WindowDoorTileEntity;
import fr.moonshade.gates.util.Functions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;


public class WindowDoor extends Block implements EntityBlock {

    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position", DoorPlacing.class,placing -> !placing.isCenterY());
    public static IntegerProperty ANIMATION = IntegerProperty.create("animation",0,4);

    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);


    public WindowDoor() {
        super(Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).noOcclusion());
        this.setRegistryName("window_door");
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
                    return Shapes.block();
            }
        }
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WindowDoorTileEntity(blockPos,blockState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,PLACING,ANIMATION);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> type) {
        return (type == ModBlock.WINDOW_DOOR_TILE_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof WindowDoorTileEntity) {
                ((WindowDoorTileEntity) t).tick(level1,blockState);
            }
        })) : null;
    }
}
