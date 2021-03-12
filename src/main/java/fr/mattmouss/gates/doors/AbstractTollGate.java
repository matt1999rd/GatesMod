package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.energystorage.IdTracker;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.SetIdPacket;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import fr.mattmouss.gates.voxels.VoxelInts;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

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
        if (!world.isRemote && !entity.isCreative()) {
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
