package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tools.VoxelInts;
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
     * on lui ajoute la clé du toll man qui permet de choisir les tarifs du péages)
     *
     *  finir la fonction getShape()
     *
     *  attention une condition pour le UP block
     *
     * petit pliiisss (ref à kaouach hamza)
     *
     * ajouter le screen de gestion du tarif et de gestion du paiement
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
    private static final VoxelShape NORTH_CLOSE_BAR;
    private static final VoxelShape SOUTH_CLOSE_BAR;
    private static final VoxelShape EAST_CLOSE_BAR;
    private static final VoxelShape WEST_CLOSE_BAR;

    private static final VoxelInts OPEN_BAR = new VoxelInts(3,0,5,4,16,6);
    private static final VoxelShape NORTH_OPEN_BAR;
    private static final VoxelShape SOUTH_OPEN_BAR;
    private static final VoxelShape EAST_OPEN_BAR;
    private static final VoxelShape WEST_OPEN_BAR;

    private static final VoxelShape NORTH_OPEN_BAR_RIGHT;
    private static final VoxelShape SOUTH_OPEN_BAR_RIGHT;
    private static final VoxelShape EAST_OPEN_BAR_RIGHT;
    private static final VoxelShape WEST_OPEN_BAR_RIGHT;

    private static final VoxelInts PLANE = new VoxelInts(0,0,5,16,16,6);
    private static final VoxelShape NORTH_PLANE;
    private static final VoxelShape SOUTH_PLANE;
    private static final VoxelShape EAST_PLANE;
    private static final VoxelShape WEST_PLANE;

    private static final VoxelInts HINGE = new VoxelInts(3,8,5,6,11,6);
    private static final VoxelShape NORTH_HINGE;
    private static final VoxelShape SOUTH_HINGE;
    private static final VoxelShape EAST_HINGE;
    private static final VoxelShape WEST_HINGE;

    private static final VoxelShape NORTH_HINGE_RIGHT;
    private static final VoxelShape SOUTH_HINGE_RIGHT;
    private static final VoxelShape EAST_HINGE_RIGHT;
    private static final VoxelShape WEST_HINGE_RIGHT;

    private static final VoxelInts BASE = new VoxelInts(1,0,0,8,11,5);
    private static final VoxelShape NORTH_BASE;
    private static final VoxelShape SOUTH_BASE;
    private static final VoxelShape EAST_BASE;
    private static final VoxelShape WEST_BASE;

    private static final VoxelShape NORTH_BASE_RIGHT;
    private static final VoxelShape SOUTH_BASE_RIGHT;
    private static final VoxelShape EAST_BASE_RIGHT;
    private static final VoxelShape WEST_BASE_RIGHT;

    private static final VoxelInts BEG_BAR = new VoxelInts(4,9,5,16,10,6);
    private static final VoxelShape NORTH_BEG_BAR;
    private static final VoxelShape SOUTH_BEG_BAR;
    private static final VoxelShape EAST_BEG_BAR;
    private static final VoxelShape WEST_BEG_BAR;

    private static final VoxelShape NORTH_BEG_BAR_RIGHT;
    private static final VoxelShape SOUTH_BEG_BAR_RIGHT;
    private static final VoxelShape EAST_BEG_BAR_RIGHT;
    private static final VoxelShape WEST_BEG_BAR_RIGHT;

    private static final VoxelInts BEG_BAR_OPEN = new VoxelInts(3,8,5,4,16,6);
    private static final VoxelShape NORTH_BEG_BAR_OPEN;
    private static final VoxelShape SOUTH_BEG_BAR_OPEN;
    private static final VoxelShape EAST_BEG_BAR_OPEN;
    private static final VoxelShape WEST_BEG_BAR_OPEN;

    private static final VoxelShape NORTH_BEG_BAR_RIGHT_OPEN;
    private static final VoxelShape SOUTH_BEG_BAR_RIGHT_OPEN;
    private static final VoxelShape EAST_BEG_BAR_RIGHT_OPEN;
    private static final VoxelShape WEST_BEG_BAR_RIGHT_OPEN;


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
            switch (facing){
                case DOWN:
                case UP:
                    throw new IllegalArgumentException("Down and Up Direction in Facing are not allowed");
                case NORTH:
                    return (tgp.getMeta()!=3)? NORTH_CLOSE_BAR :
                            ((dhs == DoorHingeSide.LEFT) ? NORTH_OPEN_BAR:NORTH_OPEN_BAR_RIGHT) ; //condition sur hinge pour open
                case SOUTH:
                    return (tgp.getMeta()!=3)? SOUTH_CLOSE_BAR :
                            ((dhs == DoorHingeSide.LEFT) ? SOUTH_OPEN_BAR:SOUTH_OPEN_BAR_RIGHT);
                case WEST:
                    return (tgp.getMeta()!=3)? WEST_CLOSE_BAR :
                            ((dhs == DoorHingeSide.LEFT) ? WEST_OPEN_BAR:WEST_OPEN_BAR_RIGHT);
                case EAST:
                    return (tgp.getMeta()!=3)? EAST_CLOSE_BAR :
                            ((dhs == DoorHingeSide.LEFT) ? EAST_OPEN_BAR:EAST_OPEN_BAR_RIGHT);
            }
        }
        //cas ou on a le block empty base qui correspond à la charnière dans l'état ouvert et fermée
        if (tgp.getMeta()==1 && (animation==0 || animation==4)){
            VoxelShape base_shape;
            VoxelShape hinge_shape;
            VoxelShape beg_bar_shape;
            switch (facing){
                case DOWN:
                case UP:
                default:
                    throw new IllegalArgumentException("No up or down direction authorised here !");
                case NORTH:
                    base_shape = (dhs == DoorHingeSide.LEFT)? NORTH_BASE : NORTH_BASE_RIGHT;
                    hinge_shape = (dhs == DoorHingeSide.LEFT)? NORTH_HINGE : NORTH_HINGE_RIGHT;
                    if (animation == 0){
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? NORTH_BEG_BAR : NORTH_BEG_BAR_RIGHT;
                    }else {
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? NORTH_BEG_BAR_OPEN : NORTH_BEG_BAR_RIGHT_OPEN;
                    }

                    break;
                case SOUTH:
                    base_shape = (dhs == DoorHingeSide.LEFT)? SOUTH_BASE : SOUTH_BASE_RIGHT;
                    hinge_shape = (dhs == DoorHingeSide.LEFT)? SOUTH_HINGE : SOUTH_HINGE_RIGHT;
                    if (animation == 0){
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? SOUTH_BEG_BAR : SOUTH_BEG_BAR_RIGHT;
                    }else {
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? SOUTH_BEG_BAR_OPEN : SOUTH_BEG_BAR_RIGHT_OPEN;
                    }
                    break;
                case WEST:
                    base_shape = (dhs == DoorHingeSide.LEFT)? WEST_BASE : WEST_BASE_RIGHT;
                    hinge_shape = (dhs == DoorHingeSide.LEFT)? WEST_HINGE : WEST_HINGE_RIGHT;
                    if (animation == 0){
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? WEST_BEG_BAR : WEST_BEG_BAR_RIGHT;
                    }else {
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? WEST_BEG_BAR_OPEN : WEST_BEG_BAR_RIGHT_OPEN;
                    }
                    break;
                case EAST:
                    base_shape = (dhs == DoorHingeSide.LEFT)? EAST_BASE : EAST_BASE_RIGHT;
                    hinge_shape = (dhs == DoorHingeSide.LEFT)? EAST_HINGE : EAST_HINGE_RIGHT;
                    if (animation == 0){
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? EAST_BEG_BAR : EAST_BEG_BAR_RIGHT;
                    }else {
                        beg_bar_shape = (dhs == DoorHingeSide.LEFT)? EAST_BEG_BAR_OPEN : EAST_BEG_BAR_RIGHT_OPEN;
                    }
                    break;
            }
            //association des trois voxelshape
            return VoxelShapes.or(base_shape,hinge_shape,beg_bar_shape);
        }
        switch (facing){
            case DOWN:
            case UP:
            default:
                throw new IllegalArgumentException("Down and Up Direction in Facing are not allowed");
            case NORTH:
                return NORTH_PLANE; //condition à faire
            case SOUTH:
                return SOUTH_PLANE;
            case WEST:
                return WEST_PLANE;
            case EAST:
                return EAST_PLANE;
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
            //à corriger pour marcher avec le schéma cu en avant et empty_base - main - empty_ext
            Direction direction = ModBlock.getDirectionFromEntity(entity,pos);
            DoorHingeSide dhs = ModBlock.getHingeSideFromEntity(entity,pos,direction);
            Direction extDirection = ModBlock.getDirectionOfExtBlock(direction,dhs);
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
        //ancienne fonctionnalité du block

        TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
        assert tgte != null;
        /*

        tgte.startAllAnimation();
        return ActionResultType.SUCCESS;

         */


        if (state.get(TG_POSITION) != TollGPosition.CONTROL_UNIT){
            return ActionResultType.FAIL;
        }
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = ModBlock.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = state.get(BlockStateProperties.DOOR_HINGE);
        //le joueur est un controleur
        if ((entity_looking_direction==facing.rotateY() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.rotateYCCW() && (dhs == DoorHingeSide.LEFT))){
            System.out.println("le joueur est un controleur ");
            tgte.openGui();
            return ActionResultType.SUCCESS;
        }
        //le joueur est un utilisateur
        if ((entity_looking_direction==facing.rotateYCCW() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.rotateY() && (dhs == DoorHingeSide.LEFT))){
            System.out.println("le joueur est un utilisateur ");
            System.out.println("openning user gui !!");
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) entity, tgte, tgte.getPos());
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;




    }



    static {

        //centre de la barrière avec le facing dans le cas fermé
        NORTH_CLOSE_BAR = Block.makeCuboidShape(0.0D,  9.0D, 5.0D,  16.0D, 10.0D, 6.0D);
        WEST_CLOSE_BAR  = Block.makeCuboidShape(5.0D,  9.0D, 0.0D,  6.0D,  10.0D, 16.0D);
        SOUTH_CLOSE_BAR = Block.makeCuboidShape(0.0D,  9.0D, 10.0D, 16.0D, 10.0D, 11.0D);
        EAST_CLOSE_BAR  = Block.makeCuboidShape(10.0D, 9.0D, 0.0D,  11.0D, 10.0D, 16.0D);

        //block up de la barrière avec le facing dans le cas ouvert
        NORTH_OPEN_BAR = Block.makeCuboidShape(3.0D,  0.0D, 5.0D,  4.0D,  16.0D, 6.0D);
        WEST_OPEN_BAR  = Block.makeCuboidShape(5.0D,  0.0D, 12.0D, 6.0D,  16.0D, 13.0D);
        SOUTH_OPEN_BAR = Block.makeCuboidShape(12.0D, 0.0D, 10.0D, 13.0D,  16.0D, 11.0D);
        EAST_OPEN_BAR  = Block.makeCuboidShape(10.0D, 0.0D, 3.0D,  11.0D, 16.0D, 4.0D);

        //block up de la barrière avec le facing dans le cas ouvert et hinge right
        NORTH_OPEN_BAR_RIGHT = Block.makeCuboidShape(12.0D,  0.0D, 5.0D,  13.0D,  16.0D, 6.0D);
        WEST_OPEN_BAR_RIGHT  = Block.makeCuboidShape(5.0D,  0.0D, 3.0D, 6.0D,  16.0D, 4.0D);
        SOUTH_OPEN_BAR_RIGHT = Block.makeCuboidShape(3.0D, 0.0D, 10.0D, 4.0D,  16.0D, 11.0D);
        EAST_OPEN_BAR_RIGHT  = Block.makeCuboidShape(10.0D, 0.0D, 12.0D,  11.0D, 16.0D, 13.0D);

        //plan de la barrière en mouvement
        NORTH_PLANE = Block.makeCuboidShape(0.0D, 0.0D,5.0D, 16.0D,16.0D,6.0D);
        WEST_PLANE  = Block.makeCuboidShape(5.0D, 0.0D,0.0D, 6.0D, 16.0D,16.0D);
        SOUTH_PLANE = Block.makeCuboidShape(0.0D, 0.0D,10.0D,16.0D,16.0D,11.0D);
        EAST_PLANE  = Block.makeCuboidShape(10.0D,0.0D,0.0D, 11.0D,16.0D,16.0D);

        //charnières
        //cas d'une charnière gauche
        NORTH_HINGE= Block.makeCuboidShape(3.0D,8.0D,5.0D,6.0D,11.0D,6.0D);
        SOUTH_HINGE= Block.makeCuboidShape(10.0D,8.0D,10.0D,13.0D,11.0D,11.0D);
        WEST_HINGE= Block.makeCuboidShape(5.0D,8.0D,10.0D,6.0D,11.0D,13.0D);
        EAST_HINGE= Block.makeCuboidShape(10.0D,8.0D,3.0D,11.0D,11.0D,6.0D);

        //cas d'une charnière droite
        NORTH_HINGE_RIGHT= Block.makeCuboidShape(10.0D,8.0D,5.0D,13.0D,11.0D,6.0D);
        EAST_HINGE_RIGHT= Block.makeCuboidShape(10.0D,8.0D,10.0D,11.0D,11.0D,13.0D);
        SOUTH_HINGE_RIGHT= Block.makeCuboidShape(3.0D,8.0D,10.0D,6.0D,11.0D,11.0D);
        WEST_HINGE_RIGHT= Block.makeCuboidShape(5.0D,8.0D,3.0D,6.0D,11.0D,6.0D);

        //base
        //cas d'une charnière gauche
        NORTH_BASE =Block.makeCuboidShape(1.0D,0.0D,0.0D,8.0D,11.0D,5.0D);
        EAST_BASE =Block.makeCuboidShape(11.0D,0.0D,1.0D,16.0D,11.0D,8.0D);
        SOUTH_BASE =Block.makeCuboidShape(8.0D,0.0D,11.0D,15.0D,11.0D,16.0D);
        WEST_BASE =Block.makeCuboidShape(0.0D,0.0D,8.0D,5.0D,11.0D,15.0D);

        //cas d'une charnière droite
        NORTH_BASE_RIGHT =Block.makeCuboidShape(8.0D,0.0D,0.0D,15.0D,11.0D,5.0D);
        SOUTH_BASE_RIGHT =Block.makeCuboidShape(1.0D,0.0D,11.0D,8.0D,11.0D,16.0D);
        EAST_BASE_RIGHT =Block.makeCuboidShape(11.0D,0.0D,8.0D,16.0D,11.0D,15.0D);
        WEST_BASE_RIGHT =Block.makeCuboidShape(0.0D,0.0D,1.0D,5.0D,11.0D,8.0D);

        //debut de la barrière
        //cas d'une charnière gauche
        NORTH_BEG_BAR =Block.makeCuboidShape(4.0D,9.0D,5.0D,16.0D,10.0D,6.0D);
        SOUTH_BEG_BAR =Block.makeCuboidShape(0.0D,9.0D,10.0D,12.0D,10.0D,11.0D);
        EAST_BEG_BAR =Block.makeCuboidShape(10.0D,9.0D,4.0D,11.0D,10.0D,16.0D);
        WEST_BEG_BAR =Block.makeCuboidShape(5.0D,9.0D,0.0D,6.0D,10.0D,12.0D);

        //cas d'une charnière droite
        NORTH_BEG_BAR_RIGHT =Block.makeCuboidShape(0.0D,9.0D,5.0D,12.0D,10.0D,6.0D);
        SOUTH_BEG_BAR_RIGHT =Block.makeCuboidShape(4.0D,9.0D,10.0D,16.0D,10.0D,11.0D);
        EAST_BEG_BAR_RIGHT =Block.makeCuboidShape(10.0D,9.0D,0.0D,11.0D,10.0D,12.0D);
        WEST_BEG_BAR_RIGHT =Block.makeCuboidShape(5.0D,9.0D,4.0D,6.0D,10.0D,16.0D);

        //cas d'une charnière gauche élevé
        NORTH_BEG_BAR_OPEN =Block.makeCuboidShape(3.0D,8.0D,5.0D,4.0D,16.0D,6.0D);
        SOUTH_BEG_BAR_OPEN =Block.makeCuboidShape(12.0D,8.0D,10.0D,13.0D,16.0D,11.0D);
        EAST_BEG_BAR_OPEN =Block.makeCuboidShape(10.0D,8.0D,3.0D,11.0D,16.0D,4.0D);
        WEST_BEG_BAR_OPEN =Block.makeCuboidShape(5.0D,8.0D,12.0D,6.0D,9.0D,13.0D);

        //cas d'une charnière droite élevé
        NORTH_BEG_BAR_RIGHT_OPEN =Block.makeCuboidShape(12.0D,8.0D,5.0D,13.0D,16.0D,6.0D);
        SOUTH_BEG_BAR_RIGHT_OPEN =Block.makeCuboidShape(3.0D,8.0D,10.0D,4.0D,16.0D,11.0D);
        EAST_BEG_BAR_RIGHT_OPEN =Block.makeCuboidShape(10.0D,8.0D,12.0D,11.0D,16.0D,13.0D);
        WEST_BEG_BAR_RIGHT_OPEN =Block.makeCuboidShape(5.0D,8.0D,3.0D,6.0D,16.0D,4.0D);

        //shape vide
        EMPTY_AABB = Block.makeCuboidShape(0.0D,0.0D,0.0D,0.0D,0.0D,0.0D);
        //shape complète
        CTRLUNITAABB = Block.makeCuboidShape(0.0D,0.0D,0.0D,16.0D,15.0D,16.0D);
    }
}
