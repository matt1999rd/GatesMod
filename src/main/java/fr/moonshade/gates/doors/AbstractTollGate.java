package fr.moonshade.gates.doors;

import fr.moonshade.gates.enum_door.TollGPosition;
import fr.moonshade.gates.util.Functions;
import fr.moonshade.gates.voxels.VoxelDoubles;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fr.moonshade.gates.enum_door.TollGPosition.*;


import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractTollGate extends Block implements EntityBlock {

    public AbstractTollGate() {
        super(Properties.of(Material.METAL)
                //1.15
                //.notSolid()
                .noOcclusion()
                .sound(SoundType.METAL)
                .strength(3.0f));

    }

    public static EnumProperty<TollGPosition> TG_POSITION ;
    public static IntegerProperty ANIMATION;
    private static final VoxelShape CTRL_UNIT_AABB;
    private static final VoxelShape EMPTY_AABB;

    private static final VoxelDoubles CLOSE_BAR = new VoxelDoubles(0,9,5,16,10,6,false);
    private static final VoxelDoubles OPEN_BAR = new VoxelDoubles(3,0,5,4,16,6,false);
    private static final VoxelDoubles PLANE = new VoxelDoubles(0,0,5,16,16,6,false);
    private static final VoxelDoubles HINGE = new VoxelDoubles(3,8,5,6,11,6,false);
    private static final VoxelDoubles BASE = new VoxelDoubles(1,0,0,8,11,5,false);
    private static final VoxelDoubles BEG_BAR = new VoxelDoubles(4,9,5,16,10,6,false);
    private static final VoxelDoubles BEG_BAR_OPEN = new VoxelDoubles(3,8,5,4,16,6,false);

    static  {
        TG_POSITION = EnumProperty.create("tg_position",TollGPosition.class);
        ANIMATION = IntegerProperty.create("animation",0,4);
    }

    //1.14.4 function replaced by notSolid()

/*
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
*/
    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        TollGPosition tgp = state.getValue(TG_POSITION);
        int animation = state.getValue(ANIMATION);
        DoorHingeSide dhs= state.getValue(BlockStateProperties.DOOR_HINGE);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (tgp.getMeta()==4){
            return CTRL_UNIT_AABB;
        }
        if (tgp.isEmpty(animation)){
            return EMPTY_AABB;
        }
        if (tgp.isSimpleBarrier(animation)){
            return getSimpleBarrierShape(facing,dhs,tgp);
        }
        //case where we are handling the block empty base which correspond to hinge at state close and open
        if (tgp.getMeta()==1 && (animation==0 || animation==4)){
            VoxelShape base_shape = getEmptyBaseShape(dhs,facing,BASE);
            VoxelShape hinge_shape = getEmptyBaseShape(dhs,facing,HINGE);
            VoxelDoubles beg_bar_VoxInt = (animation == 0) ? BEG_BAR : BEG_BAR_OPEN;
            VoxelShape beg_bar_shape = getEmptyBaseShape(dhs,facing,beg_bar_VoxInt) ;
            //association of the three voxel-shapes
            return Shapes.or(base_shape,hinge_shape,beg_bar_shape);
        }
        return PLANE.rotate(Direction.NORTH,facing).getAssociatedShape();
    }

    private VoxelShape getEmptyBaseShape(DoorHingeSide dhs, Direction facing, VoxelDoubles voxelDoubles) {
        boolean isSymmetryNeeded = (dhs == DoorHingeSide.RIGHT);
        Direction.Axis facing_axis = facing.getAxis();
        if (isSymmetryNeeded){
            return voxelDoubles.rotate(Direction.NORTH,facing).makeSymmetry(Direction.Axis.Y,facing_axis).getAssociatedShape();
        }else {
            return voxelDoubles.rotate(Direction.NORTH,facing).getAssociatedShape();
        }
    }

    private VoxelShape getSimpleBarrierShape(Direction facing, DoorHingeSide dhs,TollGPosition tgp) {
        Direction.Axis axis_symmetry = facing.getAxis();
        boolean isSymmetryNeeded = (dhs == DoorHingeSide.RIGHT);
        //if the part selected is openBarrier
        VoxelDoubles voxelDoubles = (tgp.getMeta() == 3) ? OPEN_BAR : CLOSE_BAR;
        if (isSymmetryNeeded){
            return voxelDoubles.rotate(Direction.NORTH,facing).makeSymmetry(Direction.Axis.Y,axis_symmetry).getAssociatedShape();
        }else {
            return voxelDoubles.rotate(Direction.NORTH,facing).getAssociatedShape();
        }
    }

    @Override
    public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity entity, @Nonnull ItemStack stack) {
        //we place the main block where the clicked position is defined
        //the barrier hinge parameter is defined according to the position of the player when he placed the block
        if (entity != null){
            Direction direction = Functions.getDirectionFromEntity(entity,pos);
            DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,direction);
            Direction extDirection = Functions.getDirectionOfExtBlock(direction,dhs);
            //block control unit
            world.setBlockAndUpdate(pos,
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(TG_POSITION,TollGPosition.CONTROL_UNIT)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
            );
            //block empty base
            world.setBlockAndUpdate(pos.relative(direction.getOpposite()),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(TG_POSITION, EMPTY_BASE)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
            );
            //block closed part
            world.setBlockAndUpdate(pos.relative(direction.getOpposite()).relative(extDirection),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(TG_POSITION,TollGPosition.MAIN)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
            );
            //block closed foreign part
            world.setBlockAndUpdate(pos.relative(direction.getOpposite()).relative(extDirection,2),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(TG_POSITION,TollGPosition.EMPTY_EXT)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
            );
            //block above for open gates
            world.setBlockAndUpdate(pos.relative(direction.getOpposite()).above(),
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(TG_POSITION,TollGPosition.UP_BLOCK)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
            );
        }
    }

    @Override
    public void playerDestroy(@Nonnull Level world, @Nonnull Player entity, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable BlockEntity tileEntity, @Nonnull ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    public void playerWillDestroy(Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, Player entity) {
        System.out.println("destroying all block of toll gate");
        ItemStack stack = entity.getMainHandItem();
        if (!world.isClientSide) {
            Block.dropResources(state, world, pos, null, entity, stack);
        }
        super.playerWillDestroy(world, pos, state, entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,TG_POSITION,BlockStateProperties.DOOR_HINGE,ANIMATION);
    }

    public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos, PathComputationType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.getValue(ANIMATION)==4);
            default:
                return false;
        }
    }

    public List<BlockPos> getPositionOfBlockConnected(Direction direction,TollGPosition tgp,DoorHingeSide dhs,BlockPos pos) {
        //takes in account all block including this one
        List<BlockPos> posList = new ArrayList<>();
        Direction extDirection = Functions.getDirectionOfExtBlock(direction,dhs);
        BlockPos emptyBasePos = getEmptyBasePos(tgp,extDirection,direction,pos);
        //block empty base
        posList.add(emptyBasePos);
        //block de control unit
        posList.add(emptyBasePos.relative(direction));
        //block main et empty ext
        posList.add(emptyBasePos.relative(extDirection));
        posList.add(emptyBasePos.relative(extDirection,2));
        //block up
        posList.add(emptyBasePos.above());
        return posList;
    }

    private BlockPos getEmptyBasePos(TollGPosition tgp, Direction extDirection, Direction facing,BlockPos pos) {
        switch (tgp) {
            case EMPTY_BASE:
                return pos;
            case MAIN:
                return pos.relative(extDirection.getOpposite());
            case EMPTY_EXT:
                return pos.relative(extDirection.getOpposite(), 2);
            case UP_BLOCK:
                return pos.below();
            case CONTROL_UNIT:
                return pos.relative(facing.getOpposite());
            default:
                throw new NullPointerException("TollGatePosition of block at position :" + pos + "has null attribute for tollgate position");
        }
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        if (checkFeasibility(context)){
            BlockState state = defaultBlockState();
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facing).setValue(TG_POSITION,TollGPosition.CONTROL_UNIT).setValue(ANIMATION,0);
        }else {
            return null;
        }
    }

    public static boolean checkFeasibility(BlockPlaceContext context){
        BlockPos pos =context.getClickedPos();
        Player entity = context.getPlayer();
        Level world = context.getLevel();
        assert entity != null;
        Direction facing = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,facing);
        Direction extDirection = Functions.getDirectionOfExtBlock(facing,dhs);
        List<BlockPos> posList = new ArrayList<>();
        //block control unit
        posList.add(pos);
        //block main
        posList.add(pos.relative(facing.getOpposite()));
        //blocks for closed gates
        posList.add(pos.relative(facing.getOpposite()).relative(extDirection));
        posList.add(pos.relative(facing.getOpposite()).relative(extDirection,2));
        //block for opened gates
        BlockPos ignoredPos = pos.relative(facing.getOpposite()).above();
        posList.add(pos.relative(facing.getOpposite()).above());

        for (BlockPos pos_in : posList){
            //return false if the position of this future block is occupied by another solid block
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("the blockPos that mess it up :"+pos_in);
                System.out.println("Block not working :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
            //return false if the position of this future block is above an air or bush block
            Block underBlock = world.getBlockState(pos_in.below()).getBlock();
            if (underBlock instanceof AirBlock || underBlock instanceof BushBlock || underBlock instanceof LeavesBlock){
                if (!pos_in.equals(ignoredPos)){
                    System.out.println("the blockPos that mess it up :"+pos_in.below());
                    System.out.println("No stabilising block :"+underBlock);
                    return false;
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public BlockState updateShape(BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        TollGPosition position = stateIn.getValue(TG_POSITION);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = stateIn.getValue(BlockStateProperties.DOOR_HINGE);
        if (isInnerUpdate(position,facing,blockFacing,dhs) &&  !(facingState.getBlock().getClass().equals(this.getClass()))){
            return Blocks.AIR.defaultBlockState();
        }
        if (position == CONTROL_UNIT && facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }


    //block facing is the direction of forth block
    private boolean isInnerUpdate(TollGPosition position, Direction facingUpdate, Direction blockFacing, DoorHingeSide side){
        return ( position == EMPTY_BASE && (facingUpdate == Direction.UP || facingUpdate == blockFacing || facingUpdate == ((side == DoorHingeSide.LEFT)? blockFacing.getClockWise() : blockFacing.getCounterClockWise()))) ||
                (position == CONTROL_UNIT && facingUpdate == blockFacing.getOpposite()) ||
                (position == MAIN && facingUpdate.getAxis() == blockFacing.getClockWise().getAxis()) ||
                (position == EMPTY_EXT && facingUpdate == ((side == DoorHingeSide.LEFT)?  blockFacing.getCounterClockWise() : blockFacing.getClockWise())) ||
                (position == UP_BLOCK && facingUpdate == Direction.DOWN);
    }



    static {
        //empty shape
        EMPTY_AABB = Block.box(0.0D,0.0D,0.0D,0.0D,0.0D,0.0D);
        //whole shape
        CTRL_UNIT_AABB = Block.box(0.0D,0.0D,0.0D,16.0D,15.0D,16.0D);
    }


}
