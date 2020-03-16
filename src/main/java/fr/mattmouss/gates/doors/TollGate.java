package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tools.VoxelInts;
import fr.mattmouss.gates.util.Functions;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;

import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class TollGate extends Block {

    /**
     *
     * blocktstate fait !!
     *
     * Travail à effectuer sur le block
     *
     * ajouter la fonction d'utilisation du block (lorsque le block est ajouté on l'enlève de la main du joueur et
     * on lui ajoute la clé du toll man qui permet de choisir les tarifs du péages) -> fait !

     *
     * petit pliiisss (ref à kaouach hamza)
     *
     * ajouter une possibilité de payer un ticket
     *
     * */
    public TollGate() {
        super(Properties.create(Material.BARRIER)
                .notSolid()
                .sound(SoundType.METAL)
                .hardnessAndResistance(2.0f));
        this.setRegistryName("toll_gate");
    }

    public static EnumProperty<TollGPosition> TG_POSITION ;
    public static IntegerProperty ANIMATION;
    private static final VoxelShape CTRLUNITAABB;
    private static final VoxelShape EMPTY_AABB;

    private static final VoxelInts CLOSE_BAR = new VoxelInts(0,9,5,16,10,6);
    private static final VoxelInts OPEN_BAR = new VoxelInts(3,0,5,4,16,6);
    private static final VoxelInts PLANE = new VoxelInts(0,0,5,16,16,6);
    private static final VoxelInts HINGE = new VoxelInts(3,8,5,6,11,6);
    private static final VoxelInts BASE = new VoxelInts(1,0,0,8,11,5);
    private static final VoxelInts BEG_BAR = new VoxelInts(4,9,5,16,10,6);
    private static final VoxelInts BEG_BAR_OPEN = new VoxelInts(3,8,5,4,16,6);


    static  {
        TG_POSITION = EnumProperty.create("tg_position",TollGPosition.class);
        ANIMATION = IntegerProperty.create("animation",0,4);
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TollGateTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        //on pose le block central au niveau du block selectionné
        //le coté de la barrière sera défini selon la position du joueur vis à vis du block
        if (entity != null){
            TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);

            System.out.println(tgte);
            //à corriger pour marcher avec le schéma cu en avant et empty_base - main - empty_ext
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
                    .with(TG_POSITION,TollGPosition.EMPTY_BASE)
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
        TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
        assert tgte != null;
        ItemStack stack = entity.getHeldItemMainhand();
        List<BlockPos> posList = tgte.getPositionOfBlockConnected();
        //je détruis tout les blocks à coté ainsi que le block lui même
        for (BlockPos pos1 : posList) {
            this.deleteBlock(pos1,world);
            BlockState state1= world.getBlockState(pos1);
            world.playEvent(entity,2001,pos1,Block.getStateId(state1));
            if (!world.isRemote && !entity.isCreative() && entity.canHarvestBlock(state1)) {
                Block.spawnDrops(state1, world, pos1, null, entity, stack);
            }
        }
    }

    public void deleteBlock(BlockPos pos, World world){
        world.setBlockState(pos,Blocks.AIR.getDefaultState(),35);
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
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        //old functionnality of block

        TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
        assert tgte != null;
        //we reupload the player using the gui
        tgte.changePlayerId(entity);
        /*

        tgte.startAllAnimation();
        return ActionResultType.SUCCESS;

         */
        if (state.get(TG_POSITION) != TollGPosition.CONTROL_UNIT){
            return ActionResultType.FAIL;
        }
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = state.get(BlockStateProperties.DOOR_HINGE);

        //the player is a user
        if ((entity_looking_direction==facing.rotateYCCW() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.rotateY() && (dhs == DoorHingeSide.LEFT))){
            System.out.println("the player is a user ");
            System.out.println("openning user gui !!");
            ((TollGateTileEntity) world.getTileEntity(pos)).setSide(true);
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) entity, tgte, tgte.getPos());
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    static {
        //shape vide
        EMPTY_AABB = Block.makeCuboidShape(0.0D,0.0D,0.0D,0.0D,0.0D,0.0D);
        //shape complète
        CTRLUNITAABB = Block.makeCuboidShape(0.0D,0.0D,0.0D,16.0D,15.0D,16.0D);
    }
}
