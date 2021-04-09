package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.util.Functions;
import fr.mattmouss.gates.voxels.VoxelInts;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
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

import static fr.mattmouss.gates.enum_door.TollGPosition.*;

public abstract class AbstractTollGate extends Block {

    public AbstractTollGate() {
        super(Properties.create(Material.IRON)
                //1.15
                //.notSolid()
                .sound(SoundType.METAL)
                .hardnessAndResistance(3.0f));
    }

    public static EnumProperty<TollGPosition> TG_POSITION ;
    public static IntegerProperty ANIMATION;
    private static final VoxelShape CTRLUNITAABB;
    private static final VoxelShape EMPTY_AABB;

    private static final VoxelInts CLOSE_BAR = new VoxelInts(0,9,5,16,10,6,false);
    private static final VoxelInts OPEN_BAR = new VoxelInts(3,0,5,4,16,6,false);
    private static final VoxelInts PLANE = new VoxelInts(0,0,5,16,16,6,false);
    private static final VoxelInts HINGE = new VoxelInts(3,8,5,6,11,6,false);
    private static final VoxelInts BASE = new VoxelInts(1,0,0,8,11,5,false);
    private static final VoxelInts BEG_BAR = new VoxelInts(4,9,5,16,10,6,false);
    private static final VoxelInts BEG_BAR_OPEN = new VoxelInts(3,8,5,4,16,6,false);

    static  {
        TG_POSITION = EnumProperty.create("tg_position",TollGPosition.class);
        ANIMATION = IntegerProperty.create("animation",0,4);
    }

    //1.14.4 function replaced by notSolid()

    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        TollGPosition tgp = state.get(TG_POSITION);
        int animation = state.get(ANIMATION);
        DoorHingeSide dhs= state.get(BlockStateProperties.DOOR_HINGE);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        if (tgp.getMeta()==4){
            return CTRLUNITAABB;
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
            VoxelInts beg_bar_VoxInt = (animation == 0) ? BEG_BAR : BEG_BAR_OPEN;
            VoxelShape beg_bar_shape = getEmptyBaseShape(dhs,facing,beg_bar_VoxInt) ;
            //association of the three voxelshapes
            return VoxelShapes.or(base_shape,hinge_shape,beg_bar_shape);
        }
        return PLANE.rotate(Direction.NORTH,facing).getAssociatedShape();
    }

    private VoxelShape getEmptyBaseShape(DoorHingeSide dhs, Direction facing, VoxelInts voxelInts) {
        boolean isSymetryNeeded = (dhs == DoorHingeSide.RIGHT);
        Direction.Axis facing_axis = facing.getAxis();
        if (isSymetryNeeded){
            return voxelInts.rotate(Direction.NORTH,facing).makeSymetry(Direction.Axis.Y,facing_axis).getAssociatedShape();
        }else {
            return voxelInts.rotate(Direction.NORTH,facing).getAssociatedShape();
        }
    }

    private VoxelShape getSimpleBarrierShape(Direction facing, DoorHingeSide dhs,TollGPosition tgp) {
        Direction.Axis axis_symetry = facing.getAxis();
        boolean isSymetryNeeded = (dhs == DoorHingeSide.RIGHT);
        //if the part selected is openBarrier
        VoxelInts voxelInts = (tgp.getMeta() == 3) ? OPEN_BAR : CLOSE_BAR;
        if (isSymetryNeeded){
            return voxelInts.rotate(Direction.NORTH,facing).makeSymetry(Direction.Axis.Y,axis_symetry).getAssociatedShape();
        }else {
            return voxelInts.rotate(Direction.NORTH,facing).getAssociatedShape();
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        //on pose le block central au niveau du block selectionné
        //le coté de la barrière sera défini selon la position du joueur vis à vis du block
        if (entity != null){
            Direction direction = Functions.getDirectionFromEntity(entity,pos);
            DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,direction);
            Direction extDirection = Functions.getDirectionOfExtBlock(direction,dhs);
            //le block de control unit
            world.setBlockState(pos,
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(TG_POSITION,TollGPosition.CONTROL_UNIT)
                            .with(BlockStateProperties.DOOR_HINGE,dhs)
                            .with(ANIMATION,0)
            );
            //block empty base
            world.setBlockState(pos.offset(direction.getOpposite()),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(TG_POSITION, EMPTY_BASE)
                            .with(BlockStateProperties.DOOR_HINGE,dhs)
                            .with(ANIMATION,0)
            );
            //block barrière fermé
            world.setBlockState(pos.offset(direction.getOpposite()).offset(extDirection),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(TG_POSITION,TollGPosition.MAIN)
                            .with(BlockStateProperties.DOOR_HINGE,dhs)
                            .with(ANIMATION,0)
            );
            //block eloigné fermé
            world.setBlockState(pos.offset(direction.getOpposite()).offset(extDirection,2),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(TG_POSITION,TollGPosition.EMPTY_EXT)
                            .with(BlockStateProperties.DOOR_HINGE,dhs)
                            .with(ANIMATION,0)
            );
            //block au dessus
            world.setBlockState(pos.offset(direction.getOpposite()).up(),
                    state.with(BlockStateProperties.HORIZONTAL_FACING,direction)
                            .with(TG_POSITION,TollGPosition.UP_BLOCK)
                            .with(BlockStateProperties.DOOR_HINGE,dhs)
                            .with(ANIMATION,0)
            );
        }
    }

    @Override
    public void harvestBlock(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.harvestBlock(world, entity, pos, Blocks.AIR.getDefaultState(), tileEntity, stack);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of toll gate");
        ItemStack stack = entity.getHeldItemMainhand();
        if (!world.isRemote) {
            Block.spawnDrops(state, world, pos, null, entity, stack);
        }
        super.onBlockHarvested(world, pos, state, entity);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,TG_POSITION,BlockStateProperties.DOOR_HINGE,ANIMATION);
    }

    public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.get(ANIMATION)==4);
            default:
                return false;
        }
    }

    public List<BlockPos> getPositionOfBlockConnected(Direction direction,TollGPosition tgp,DoorHingeSide dhs,BlockPos pos) {
        //ajout de tout les blocks
        List<BlockPos> posList = new ArrayList<>();
        Direction extDirection = Functions.getDirectionOfExtBlock(direction,dhs);
        BlockPos emptyBasePos = getEmptyBasePos(tgp,extDirection,direction,pos);
        //block emptybase
        posList.add(emptyBasePos);
        //block de control unit
        posList.add(emptyBasePos.offset(direction));
        //block main et emptyext
        posList.add(emptyBasePos.offset(extDirection));
        posList.add(emptyBasePos.offset(extDirection,2));
        //block up
        posList.add(emptyBasePos.up());
        return posList;
    }

    private BlockPos getEmptyBasePos(TollGPosition tgp, Direction extDirection, Direction facing,BlockPos pos) {
        switch (tgp) {
            case EMPTY_BASE:
                return pos;
            case MAIN:
                return pos.offset(extDirection.getOpposite());
            case EMPTY_EXT:
                return pos.offset(extDirection.getOpposite(), 2);
            case UP_BLOCK:
                return pos.down();
            case CONTROL_UNIT:
                return pos.offset(facing.getOpposite());
            default:
                throw new NullPointerException("TollGatePosition of block at position :" + pos + "has null attribut for tollgateposition");
        }
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction facing = context.getPlacementHorizontalFacing();
        if (checkFeasibility(context)){
            BlockState state = getDefaultState();
            return state.with(BlockStateProperties.HORIZONTAL_FACING,facing).with(TG_POSITION,TollGPosition.CONTROL_UNIT).with(ANIMATION,0);
        }else {
            return null;
        }
    }

    public static boolean checkFeasibility(BlockItemUseContext context){
        BlockPos pos =context.getPos();
        PlayerEntity entity = context.getPlayer();
        World world = context.getWorld();
        Direction facing = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,facing);
        Direction extDirection = Functions.getDirectionOfExtBlock(facing,dhs);
        List<BlockPos> posList = new ArrayList<>();
        //block de control unit
        posList.add(pos);
        //block main
        posList.add(pos.offset(facing.getOpposite()));
        //blocks de barrière fermé
        posList.add(pos.offset(facing.getOpposite()).offset(extDirection));
        posList.add(pos.offset(facing.getOpposite()).offset(extDirection,2));
        //block de barrière ouverte
        BlockPos ignoredPos = pos.offset(facing.getOpposite()).up();
        posList.add(pos.offset(facing.getOpposite()).up());

        for (BlockPos pos_in : posList){
            //return false if the position of this future block is occupied by another solid block
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("la blockPos qui fait foirer :"+pos_in);
                System.out.println("Block qui bloque :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
            //return false if the position of this future block is above a air or bush block
            Block underBlock = world.getBlockState(pos_in.down()).getBlock();
            if (underBlock instanceof AirBlock || underBlock instanceof BushBlock || underBlock instanceof LeavesBlock){
                if (!pos_in.equals(ignoredPos)){
                    System.out.println("la blockPos qui fait foirer :"+pos_in.down());
                    System.out.println("Block qui ne stabilise pas :"+underBlock);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TollGPosition position = stateIn.get(TG_POSITION);
        Direction blockFacing = stateIn.get(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = stateIn.get(BlockStateProperties.DOOR_HINGE);
        if (isInnerUpdate(position,facing,blockFacing,dhs) &&  !(facingState.getBlock().getClass().equals(this.getClass()))){
            return Blocks.AIR.getDefaultState();
        }
        if (position == CONTROL_UNIT && facing == Direction.DOWN && !facingState.getMaterial().blocksMovement()){
            return Blocks.AIR.getDefaultState();
        }
        return stateIn;
    }


    //block facing is the direction of forth block
    private boolean isInnerUpdate(TollGPosition position, Direction facingUpdate, Direction blockFacing, DoorHingeSide side){
        return ( position == EMPTY_BASE && (facingUpdate == Direction.UP || facingUpdate == blockFacing || facingUpdate == ((side == DoorHingeSide.LEFT)? blockFacing.rotateY() : blockFacing.rotateYCCW()))) ||
                (position == CONTROL_UNIT && facingUpdate == blockFacing.getOpposite()) ||
                (position == MAIN && facingUpdate.getAxis() == blockFacing.rotateY().getAxis()) ||
                (position == EMPTY_EXT && facingUpdate == ((side == DoorHingeSide.LEFT)?  blockFacing.rotateYCCW() : blockFacing.rotateY())) ||
                (position == UP_BLOCK && facingUpdate == Direction.DOWN);
    }

    static {
        //shape vide
        EMPTY_AABB = Block.makeCuboidShape(0.0D,0.0D,0.0D,0.0D,0.0D,0.0D);
        //shape complète
        CTRLUNITAABB = Block.makeCuboidShape(0.0D,0.0D,0.0D,16.0D,15.0D,16.0D);
    }


}
