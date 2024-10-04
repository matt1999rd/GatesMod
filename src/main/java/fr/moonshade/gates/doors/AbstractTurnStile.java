package fr.moonshade.gates.doors;



import fr.moonshade.gates.enum_door.TurnSPosition;
import fr.moonshade.gates.util.Functions;
import fr.moonshade.gates.voxels.VoxelDoubles;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static fr.moonshade.gates.enum_door.TurnSPosition.*;


import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractTurnStile extends Block implements EntityBlock {

    public static EnumProperty<TurnSPosition> TS_POSITION ;
    public static IntegerProperty ANIMATION;
    public static BooleanProperty WAY_IS_ON;

    static  {
        TS_POSITION = EnumProperty.create("ts_position",TurnSPosition.class);
        ANIMATION = IntegerProperty.create("animation",0,1);
        WAY_IS_ON = BooleanProperty.create("way_is_on");
    }

    public AbstractTurnStile() {
        super(Properties.of(Material.METAL)
                        .strength(2.0f)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                //1.15 function
                //.notSolid()
        );
    }


    @Override
    @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState state,  BlockGetter reader, BlockPos pos, CollisionContext context) {
        if (state.getValue(TS_POSITION)!= TurnSPosition.MAIN && state.getValue(TS_POSITION)!=TurnSPosition.UP_BLOCK){
            //we need to block the jumping of player on the turn stile which mean fraud.
            return Block.box(0,0,0,16,18,16);
        }else if (state.getValue(TS_POSITION) == TurnSPosition.UP_BLOCK){
            //we had this block because player is jumping on the turn stile without this block
            return (state.getValue(WAY_IS_ON))?Shapes.empty():makeUpBlockShape(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }

        return getTurnStileShape(state);
    }


    private VoxelShape getTurnStileShape(BlockState state){
        int anim = state.getValue(ANIMATION);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        VoxelShape shape;
        VoxelDoubles RotationBlock;
        VoxelDoubles[] ForwardPart,BackwardPart,MiddlePart;
        RotationBlock = new VoxelDoubles(0,7,6,2,10,10,false);

        if (anim == 0){
            MiddlePart = new VoxelDoubles[]{
                    new VoxelDoubles(2,10,7.4676,2,1,1,true),
                    new VoxelDoubles(4,11,7.4676,2,1,1,true),
                    new VoxelDoubles(6,12,7.4676,2,1,1,true),
                    new VoxelDoubles(8,13,7.4676,1,1,1,true),
                    new VoxelDoubles(9,14,7.4676,2,1,1,true),
                    new VoxelDoubles(11,15,7.4676,2,1,1,true)
            };
            ForwardPart = new VoxelDoubles[]{
                    new VoxelDoubles(3,7,6.5,1,1,1,true),
                    new VoxelDoubles(4,6,5.5,1,1,1,true),
                    new VoxelDoubles(5,5,4.5,2,1,1,true),
                    new VoxelDoubles(7,5,3.5,1,1,1,true),
                    new VoxelDoubles(8,4,2.5,1,1,1,true),
                    new VoxelDoubles(9,3,1.5,1,1,1,true),
                    new VoxelDoubles(10,3,0.5,1,1,1,true)
            };

            BackwardPart = new VoxelDoubles[]{
                    new VoxelDoubles(3,7,8.5,1,1,1,true),
                    new VoxelDoubles(4,6,9.5,1,1,1,true),
                    new VoxelDoubles(5,5,10.5,2,1,1,true),
                    new VoxelDoubles(7,5,11.5,1,1,1,true),
                    new VoxelDoubles(8,4,12.5,1,1,1,true),
                    new VoxelDoubles(9,3,13.5,1,1,1,true),
                    new VoxelDoubles(10,3,14.5,1,1,1,true)
            };
        }else if (anim == 1){
            MiddlePart = new VoxelDoubles[]{
                    new VoxelDoubles(1,6,8,2,1,1,true),
                    new VoxelDoubles(3,5,8,1,1,1,true),
                    new VoxelDoubles(4,4,8,2,1,1,true),
                    new VoxelDoubles(6,3,8,2,1,1,true),
                    new VoxelDoubles(8,2,8,2,1,1,true),
                    new VoxelDoubles(10,1,8,1,1,1,true),
                    new VoxelDoubles(11,0,8,2,1,1,true)
            };
            ForwardPart = new VoxelDoubles[]{
                    new VoxelDoubles(2,10.5,6,1,1,1,true),
                    new VoxelDoubles(3,11,5.5,1,1,1,true),
                    new VoxelDoubles(4,11.8,4.5,1,1,1,true),
                    new VoxelDoubles(5,12.2,4,1,1,1,true),
                    new VoxelDoubles(6,12.8,3,1,1,1,true),
                    new VoxelDoubles(7,13.4,2,1,1,1,true),
                    new VoxelDoubles(8,14,1.5,1,1,1,true),
                    new VoxelDoubles(9,14.5,0.5,1,1,1,true)
            };
            BackwardPart = new VoxelDoubles[]{
                    new VoxelDoubles(2,10.5,9,1,1,1,true),
                    new VoxelDoubles(3,11,9.5,1,1,1,true),
                    new VoxelDoubles(4,11.8,10.5,1,1,1,true),
                    new VoxelDoubles(5,12.2,11,1,1,1,true),
                    new VoxelDoubles(6,12.8,12,1,1,1,true),
                    new VoxelDoubles(7,13.4,13,1,1,1,true),
                    new VoxelDoubles(8,14,13.5,1,1,1,true),
                    new VoxelDoubles(9,14.5,14.5,1,1,1,true)
            };

        }else {
            ForwardPart = new VoxelDoubles[]{VoxelDoubles.EMPTY} ;
            BackwardPart = new VoxelDoubles[]{VoxelDoubles.EMPTY};
            MiddlePart = new VoxelDoubles[]{VoxelDoubles.EMPTY};
        }

        shape = RotationBlock.rotate(Direction.NORTH,facing).getAssociatedShape();

        for (VoxelDoubles vi : ForwardPart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = Shapes.or(shape,vi.getAssociatedShape());
        }
        for (VoxelDoubles vi : BackwardPart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = Shapes.or(shape,vi.getAssociatedShape());
        }
        for (VoxelDoubles vi : MiddlePart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = Shapes.or(shape,vi.getAssociatedShape());
        }

        return shape;
    }

    private VoxelShape makeUpBlockShape(Direction facing){
        switch (facing){
            case EAST:
                return Block.box(6,0,0,16,16,16);
            case NORTH:
                return Block.box(0,0,0,16,16,10);
            case WEST:
                return Block.box(0,0,0,10,16,16);
            case SOUTH:
                return Block.box(0,0,6,16,16,16);
            default:
                return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,TS_POSITION,ANIMATION,BlockStateProperties.DOOR_HINGE,WAY_IS_ON);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        if (checkFeasibility(context)){
            BlockState state = defaultBlockState();
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facing).setValue(TS_POSITION, TurnSPosition.MAIN).setValue(ANIMATION,0);
        }else {
            return null;
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        if (player != null){
            Direction direction = Functions.getDirectionFromEntity(player,pos);
            DoorHingeSide dhs = Functions.getHingeSideFromEntity(player,pos,direction);
            //boolean that return true when Control Unit is on the right
            boolean CUisOnRight = (dhs == DoorHingeSide.RIGHT);
            //the control unit block (left if DHS.left and right if DHS.right)
            TurnSPosition tsp = Functions.getCUPosition(pos,player);
            world.setBlockAndUpdate(pos,
                    state
                            .setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
                            .setValue(TS_POSITION,tsp)
                            .setValue(WAY_IS_ON,false)
            );
            //the main block
            //offset with rotateY is for left block and rotateYCCW is for right block regarding direction of facing
            BlockPos MainPos = Functions.getMainPosition(pos,player);
            world.setBlockAndUpdate(MainPos,
                    state
                            .setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
                            .setValue(TS_POSITION,TurnSPosition.MAIN)
                            .setValue(WAY_IS_ON,false)
            );
            //the up block
            world.setBlockAndUpdate(MainPos.relative(Direction.UP),
                    state
                            .setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
                            .setValue(TS_POSITION,TurnSPosition.UP_BLOCK)
                            .setValue(WAY_IS_ON,false)
            );

            //the right block
            BlockPos ExtremityPos = (CUisOnRight) ? pos.relative(direction.getClockWise(),2) : pos.relative(direction.getCounterClockWise(),2);
            TurnSPosition ExtremityTsp = (CUisOnRight) ? TurnSPosition.LEFT_BLOCK : TurnSPosition.RIGHT_BLOCK;
            world.setBlockAndUpdate(ExtremityPos,
                    state
                            .setValue(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .setValue(BlockStateProperties.DOOR_HINGE,dhs)
                            .setValue(ANIMATION,0)
                            .setValue(TS_POSITION,ExtremityTsp)
                            .setValue(WAY_IS_ON,false)
            );
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState downBlockState = worldIn.getBlockState(pos.below());
        if (state.getValue(TS_POSITION) == UP_BLOCK){
            Block block = downBlockState.getBlock();
            return (block == this);
        }else {
            return downBlockState.isFaceSturdy(worldIn,pos.below(),Direction.UP);
        }
    }


    @Override
    @ParametersAreNonnullByDefault
    public void playerDestroy(Level world, Player entity, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player entity) {
        System.out.println("destroying all block of turn stile");
        ItemStack stack = entity.getMainHandItem();
        if (!world.isClientSide) {
            Block.dropResources(state, world, pos, null, entity, stack);
        }
        super.playerWillDestroy(world, pos, state, entity);
    }

    @ParametersAreNonnullByDefault
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.getValue(ANIMATION)==1);
            default:
                return false;
        }
    }

    private boolean checkFeasibility(BlockPlaceContext context) {
        BlockPos pos =context.getClickedPos();
        Level world = context.getLevel();
        Player entity = context.getPlayer();
        assert entity != null;
        Direction facing = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,facing);
        Direction dir_other_block = (dhs == DoorHingeSide.RIGHT) ? facing.getClockWise() : facing.getCounterClockWise();
        List<BlockPos> posList = new ArrayList<>();
        //block Control Unit
        posList.add(pos);
        //block main
        posList.add(pos.relative(dir_other_block));
        //block opposite CU
        posList.add(pos.relative(dir_other_block,2));

        for (BlockPos pos_in : posList){
            //return false if the position of this future block is occupied by another solid block
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("the blockPos that mess it up :"+pos_in);
                System.out.println("Block not working :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
            //return false if the position of this future block is above an air or bush or leaves block
            Block underBlock = world.getBlockState(pos_in.below()).getBlock();
            if (underBlock instanceof AirBlock || underBlock instanceof BushBlock || underBlock instanceof LeavesBlock){
                System.out.println("the blockPos that mess it up :"+pos_in.below());
                System.out.println("No stabilising block :"+underBlock);
                return false;
            }
        }
        return true;

    }

    public List<BlockPos> getPositionOfBlockConnected(BlockState state,BlockPos pos) {
        BlockPos mainPos = getMainPos(state,pos);
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = state.getValue(BlockStateProperties.DOOR_HINGE);
        //include all blocks
        List<BlockPos> posList = new ArrayList<>();
        Direction leftBlockDirection=(dhs==DoorHingeSide.RIGHT)? direction.getClockWise() : direction.getCounterClockWise();
        //block main
        posList.add(mainPos);
        //block left
        posList.add(mainPos.relative(leftBlockDirection));
        //block right
        posList.add(mainPos.relative(leftBlockDirection.getOpposite()));
        //block up
        posList.add(mainPos.above());
        return posList;
    }

    public BlockPos getMainPos(BlockState state,BlockPos pos){
        TurnSPosition tsp = state.getValue(TS_POSITION);
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = state.getValue(BlockStateProperties.DOOR_HINGE);
        Direction leftBlockDirection=(dhs==DoorHingeSide.RIGHT)? direction.getClockWise() : direction.getCounterClockWise();
        switch (tsp) {
            case MAIN:
                return pos;
            case LEFT_BLOCK:
                return pos.relative(leftBlockDirection);
            case UP_BLOCK:
                return pos.below();
            case RIGHT_BLOCK:
                return pos.relative(leftBlockDirection.getOpposite());
            default:
                throw new NullPointerException("TollGatePosition of block at position :" + pos + "has null attribute for turn stile position");
        }
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        TurnSPosition position = stateIn.getValue(TS_POSITION);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing)){
            return (facingState.getBlock().getClass().equals(this.getClass()) && facingState.getValue(TS_POSITION) != position)?
                    getUpdateState(stateIn,facingState)
                    : Blocks.AIR.defaultBlockState();
        }
        if (position.isDown() && facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }

    protected abstract BlockState getUpdateState(BlockState state,BlockState facingState);

    //block facing is the direction of forth block
    private boolean isInnerUpdate(TurnSPosition position, Direction facingUpdate, Direction blockFacing){
        return ((position == RIGHT_BLOCK && facingUpdate == blockFacing.getClockWise()) ||
                (position == LEFT_BLOCK && facingUpdate == blockFacing.getCounterClockWise()) ||
                (position == MAIN && (facingUpdate.getAxis() == blockFacing.getClockWise().getAxis() || facingUpdate == Direction.UP) ) ||
                (position == UP_BLOCK && facingUpdate == Direction.DOWN));
    }

    //check if this TE is the control unit tile entity to avoid multiple definition of id storage that will be of no use
    public boolean isControlUnit(BlockState state) {
        DoorHingeSide dhs = state.getValue(BlockStateProperties.DOOR_HINGE);
        return (dhs == DoorHingeSide.RIGHT) ?
                state.getValue(TurnStile.TS_POSITION) == TurnSPosition.RIGHT_BLOCK :
                state.getValue(TurnStile.TS_POSITION) == TurnSPosition.LEFT_BLOCK;
    }


}
