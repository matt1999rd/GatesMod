package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.enum_door.Placing;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.tileentity.GarageTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
// for 1.14
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class GarageDoor extends Block implements EntityBlock {

    /**
    * one thing to do :
     **display in chat how to use the garage door
     * */

    public GarageDoor(String key) {
        super(Properties.of(Material.METAL)
        .strength(2.0f)
        .lightLevel(value -> 0)
        .sound(SoundType.METAL)
        .noOcclusion()
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




    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GarageTileEntity(blockPos,blockState);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, GARAGE_PLACING,ANIMATION);
    }



    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            Direction dir_entity = Functions.getDirectionFromEntity(entity,pos);
            System.out.println("dir_entity :"+dir_entity.getSerializedName());
            //placement of the 2 block on the right of the garage door
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.DOWN_RIGHT)
                    .setValue(ANIMATION,0));
            world.setBlockAndUpdate(pos.above(),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.UP_RIGHT)
                    .setValue(ANIMATION,0));
            //placement of the 2 block on the left of the garage door
            Direction dir_left_section=dir_entity.getClockWise();
            world.setBlockAndUpdate(pos.relative(dir_left_section),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.DOWN_LEFT)
                    .setValue(ANIMATION,0));
            world.setBlockAndUpdate(pos.relative(dir_left_section).above(),state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING,dir_entity)
                    .setValue(GARAGE_PLACING,Placing.UP_LEFT)
                    .setValue(ANIMATION,0));
            //placement of the 2 block that makes the opened garage door
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
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player playerEntity) {
        ItemStack itemstack = playerEntity.getMainHandItem();
        if (!world.isClientSide && !playerEntity.isCreative()) {
            Block.dropResources(state, world, pos, null, playerEntity, itemstack);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void playerDestroy(Level world, Player entity, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        Placing placing = stateIn.getValue(GARAGE_PLACING);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();

        if (isInnerUpdate(placing,facing,blockFacing) && !(facingState.getBlock() instanceof GarageDoor)){
            System.out.println("no other garage part found for placing :"+ placing +" Block will be destroyed");
            return Blocks.AIR.defaultBlockState();
        }

        if (isSupportUpdate(placing,facing,blockFacing) && !facingState.getMaterial().blocksMotion()){
            System.out.println("no support found for placing :"+ placing +" Block will be destroyed");
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
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult blockRayTraceResult) {
        GarageTileEntity gte = (GarageTileEntity) world.getBlockEntity(pos);
        System.out.println("position of base block:"+pos);
        assert gte != null;
        List<BlockPos> posList = gte.getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            if (!(world.getBlockEntity(pos1) instanceof GarageTileEntity)) throw new IllegalArgumentException("No tile entity on this blockPos :"+pos1);
            System.out.println("position of animated block :"+pos1);
            GarageTileEntity gte2 = (GarageTileEntity) world.getBlockEntity(pos1);
            assert gte2 != null;
            gte2.startAnimation();
        }
        gte.startAnimation();
        return InteractionResult.SUCCESS;
    }

    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.getValue(GarageDoor.ANIMATION)==5);
            default:
                return false;
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (type == ModBlock.GARAGE_TILE_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof GarageTileEntity) {
                ((GarageTileEntity) t).tick(level1);
            }
        })) : null;
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
