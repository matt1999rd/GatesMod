package fr.mattmouss.gates.doors;



import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.util.Functions;
import fr.mattmouss.gates.voxels.VoxelDoubles;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
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
import java.util.ArrayList;
import java.util.List;

import static fr.mattmouss.gates.enum_door.TurnSPosition.*;


public abstract class AbstractTurnStile extends Block {

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
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        if (state.getValue(TS_POSITION)!= TurnSPosition.MAIN && state.getValue(TS_POSITION)!=TurnSPosition.UP_BLOCK){
            //we need to block the jumping of player on the turn stile which mean fraud.
            return Block.box(0,0,0,16,18,16);
        }else if (state.getValue(TS_POSITION) == TurnSPosition.UP_BLOCK){
            //we had this block because player is jumping on the turn stile without this block
            return (state.getValue(WAY_IS_ON))?VoxelShapes.empty():makeUpBlockShape(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }

        return getTurnStileShape(state);
    }


    //1.14.4 function replaced by notSolid()
/*
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
*/


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
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }
        for (VoxelDoubles vi : BackwardPart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }
        for (VoxelDoubles vi : MiddlePart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,TS_POSITION,ANIMATION,BlockStateProperties.DOOR_HINGE,WAY_IS_ON);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction facing = context.getHorizontalDirection();
        if (checkFeasibility(context)){
            BlockState state = defaultBlockState();
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facing).setValue(TS_POSITION, TurnSPosition.MAIN).setValue(ANIMATION,0);
        }else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
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
    public void playerDestroy(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of turn stile");
        ItemStack stack = entity.getMainHandItem();
        if (!world.isClientSide) {
            Block.dropResources(state, world, pos, null, entity, stack);
        }
        super.playerWillDestroy(world, pos, state, entity);
    }

    public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.getValue(ANIMATION)==1);
            default:
                return false;
        }
    }

    private boolean checkFeasibility(BlockItemUseContext context) {
        BlockPos pos =context.getClickedPos();
        World world = context.getLevel();
        PlayerEntity entity = context.getPlayer();
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

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TurnSPosition position = stateIn.getValue(TS_POSITION);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing) &&  !(facingState.getBlock().getClass().equals(this.getClass()))){
            return Blocks.AIR.defaultBlockState();
        }
        if (position.isDown() && facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }

    //block facing is the direction of forth block
    private boolean isInnerUpdate(TurnSPosition position, Direction facingUpdate, Direction blockFacing){
        return ((position == RIGHT_BLOCK && facingUpdate == blockFacing.getCounterClockWise()) ||
                (position == LEFT_BLOCK && facingUpdate == blockFacing.getClockWise()) ||
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
