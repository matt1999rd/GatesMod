package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.enum_door.Placing;
import fr.mattmouss.gates.tileentity.GarageTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
// for 1.14
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


public class GarageDoor extends Block {

    /**Plusieurs Bugs repèré :
     *
     * le blockItem ne vérifie la possibilité de poser le block de garage et supprime simplement les blocks présents à cet endroit
     *  (corrigé)
     *
     * lorsque deux blocks de garage sont présent il s'ouvre en même temps au lieu d'être dissocié (Animation variable trop globale)
     * (corrigé : capability + pas de variable static dans la classe encapsulé)
     *
     * bug d'animation
     * (due au problème 2)
     *
     * **/

    /**
    * quelques trucs à faire encore :
    ** ajouter une gestion de la stabilité du block :
     * le block doit se défaire si l'un des deux blocks en dessous devient de l'air (fait !!)
     **afficher sur le chat comment poser le garage
     * */

    public GarageDoor(String key) {
        super(Properties.of(Material.METAL)
        .strength(2.0f)
        .lightLevel(value -> 0)
        .sound(SoundType.METAL)
        //.notSolid()
        );
        this.setRegistryName(key);
    }

    public static EnumProperty<Placing> GARAGE_PLACING ;
    public static IntegerProperty ANIMATION;
    private static final VoxelShape SOUTH_AABB;
    private static final VoxelShape NORTH_AABB;
    private static final VoxelShape EAST_AABB;
    private static final VoxelShape WEST_AABB;
    private static final VoxelShape EMPTY_AABB;
    private static final VoxelShape UP_AABB;
    private static final VoxelShape FULL_AABB;

    static  {
        GARAGE_PLACING = EnumProperty.create("placing",Placing.class);
        ANIMATION = IntegerProperty.create("animation",0,5);
    }

/*
    //1.14.4 function replaced by notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }


 */




    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        Placing placing = state.getValue(GARAGE_PLACING);
        int val = state.getValue(ANIMATION);
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch (placing){
            case DOWN_LEFT:
            case DOWN_RIGHT:
                return getDOWNShape(val,dir);
            case UP_LEFT:
            case UP_RIGHT:
                return getUPShape(val,dir);
            case BACK_LEFT:
            case BACK_RIGHT:
                return getBACKShape(val);
            default:
                throw new IllegalStateException("Unexpected value: " + placing);
        }
    }

    private VoxelShape getDOWNShape(int val,Direction dir) {
        if (val > 2){
            return EMPTY_AABB;
        }else {
            switch (dir){
                case NORTH:
                    return SOUTH_AABB;
                case SOUTH:
                    return NORTH_AABB;
                case WEST:
                    return EAST_AABB;
                case EAST:
                    return WEST_AABB;
                default:
                    throw new IllegalArgumentException("no such direction authorized");
            }
        }

    }

    private VoxelShape getBACKShape(int val){
        if (val<3){
            return EMPTY_AABB;
        }else {
            return UP_AABB;
        }
    }

    private VoxelShape getUPShape(int val,Direction dir){
        switch (val){
            case 0:
                return getDOWNShape(val,dir);
            case 1:
                switch (dir){
                    case DOWN:
                    case UP:
                        throw new IllegalArgumentException("no such direction up and down authorised");
                    case NORTH:
                        Block.box(
                                0.0D,0.0D,0.0D,
                                16.0D,15.0D,7.0D
                        );
                    case SOUTH:
                        Block.box(
                                0.0D,0.0D,9.0D,
                                16.0D,15.0D,16.0D
                        );
                    case WEST:
                        Block.box(
                                0.0D,0.0D,0.0D,
                                7.0D,15.0D,16.0D
                        );
                    case EAST:
                        Block.box(
                                9.0D,0.0D,0.0D,
                                16.0D,15.0D,16.0D
                        );
                }
            case 2:
            case 3:
                return FULL_AABB;
            case 4:
                return Block.box(
                        0.0D,9.0D,0.0D,
                        16.0D,17.0D,17.0D
                );
            case 5:
                return UP_AABB;
            default:
                throw new IllegalArgumentException("No such number allowed for animation");
        }

    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new GarageTileEntity();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, GARAGE_PLACING,ANIMATION);
    }



    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            Direction dir_entity = Functions.getDirectionFromEntity(entity,pos);
            System.out.println("dir_entity :"+dir_entity.getSerializedName());
            //placement des 2 block de droite de la porte de garage
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.DOWN_RIGHT)
                    .setValue(ANIMATION,0));
            world.setBlockAndUpdate(pos.above(),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.UP_RIGHT)
                    .setValue(ANIMATION,0));
            //placement des 2 block à gauche de la porte de garage
            Direction dir_left_section=dir_entity.getClockWise();
            world.setBlockAndUpdate(pos.relative(dir_left_section),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.DOWN_LEFT)
                    .setValue(ANIMATION,0));
            world.setBlockAndUpdate(pos.relative(dir_left_section).above(),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.UP_LEFT)
                    .setValue(ANIMATION,0));
            //placement des 2 block qui constitue le garage ouvert
            world.setBlockAndUpdate(pos.relative(dir_entity.getOpposite()).above(),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.BACK_RIGHT)
                    .setValue(ANIMATION,0));
            world.setBlockAndUpdate(pos.relative(dir_entity.getOpposite()).relative(dir_left_section).above(),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.BACK_LEFT)
                    .setValue(ANIMATION,0));
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity playerEntity) {
        ItemStack itemstack = playerEntity.getMainHandItem();
        if (!world.isClientSide && !playerEntity.isCreative()) {
            Block.dropResources(state, world, pos, null, playerEntity, itemstack);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void playerDestroy(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        Placing placing = stateIn.getValue(GARAGE_PLACING);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();

        if (isInnerUpdate(placing,facing,blockFacing) && !(facingState.getBlock() instanceof GarageDoor)){
            System.out.println("no other garage part found for placing :"+placing.toString()+" Block will be destroyed");
            return Blocks.AIR.defaultBlockState();
        }

        if (isSupportUpdate(placing,facing,blockFacing) && !facingState.getMaterial().blocksMotion()){
            System.out.println("no support found for placing :"+placing.toString()+" Block will be destroyed");
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn,facing,facingState,worldIn,currentPos,facingPos);
    }


    //block facing is the direction of back block
    private boolean isInnerUpdate(Placing placing, Direction facingUpdate, Direction blockFacing){
        return ( placing.isUpFace() && (facingUpdate == Direction.DOWN || facingUpdate == blockFacing)) ||
                (placing.isUpBack() && facingUpdate == blockFacing.getOpposite()) ||
                (!placing.isUp() && facingUpdate == Direction.UP) ||
                (placing.isRight() && facingUpdate == blockFacing.getCounterClockWise()) ||
                (!placing.isRight() && facingUpdate == blockFacing.getClockWise());
    }


    private boolean isSupportUpdate(Placing placing, Direction facingUpdate,Direction blockFacing){
        return ( placing.isUp() && facingUpdate == Direction.UP) ||
                (!placing.isUp() && facingUpdate == Direction.DOWN) ||
                (placing.isRight() && facingUpdate == blockFacing.getClockWise()) ||
                (!placing.isRight() && facingUpdate == blockFacing.getCounterClockWise());
    }

    //1.15 function

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        GarageTileEntity gte = (GarageTileEntity) world.getBlockEntity(pos);
        System.out.println("position du block de base :"+pos);
        assert gte != null;
        List<BlockPos> posList = gte.getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            if (!(world.getBlockEntity(pos1) instanceof GarageTileEntity)) throw new IllegalArgumentException("No tile entity on this blockPos :"+pos1);
            System.out.println("position du block animé :"+pos1);
            GarageTileEntity gte2 = (GarageTileEntity) world.getBlockEntity(pos1);
            assert gte2 != null;
            gte2.startAnimation();
        }
        gte.startAnimation();
        return ActionResultType.SUCCESS;
    }






/*
    //1.14.4 function for onBlockActivated

    @Override
    public boolean func_220051_a(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand p_220051_5_, BlockRayTraceResult p_220051_6_) {
        GarageTileEntity gte = (GarageTileEntity) world.getBlockEntity(pos);
        System.out.println("position du block de base :"+pos);
        assert gte != null;
        List<BlockPos> posList = gte.getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            if (!(world.getBlockEntity(pos1) instanceof GarageTileEntity)) throw new IllegalArgumentException("No tile entity on this blockPos :"+pos1);
            System.out.println("position du block animé :"+pos1);
            GarageTileEntity gte2 = (GarageTileEntity) world.getBlockEntity(pos1);
            assert gte2 != null;
            gte2.startAnimation();
        }
        gte.startAnimation();
        return true;
    }


 */



    public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.getValue(GarageDoor.ANIMATION)==5);
            default:
                return false;
        }
    }


    static {
        SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
        NORTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
        WEST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
        EMPTY_AABB = Block.box(0.0D,0.0D,0.0D,0.0D,0.0D,0.0D);
        UP_AABB = Block.box(0.0D,15.0D,0.0D,16.0D,16.0D,16.0D);
        FULL_AABB = Block.box(0.0D,0.0D,0.0D,16.0D,16.0D,16.0D);
    }


}
