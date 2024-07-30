package fr.mattmouss.gates.windows;

import com.mojang.math.Vector3d;
import fr.mattmouss.gates.util.ExtendDirection;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.world.level.material.Material;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
//import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class WindowBlock extends Block {

    public static EnumProperty<WindowPlace> WINDOW_PLACE;
    public static BooleanProperty ROTATED;

    protected static final VoxelShape SOUTH_AABB;
    protected static final VoxelShape NORTH_AABB;
    protected static final VoxelShape WEST_AABB;
    protected static final VoxelShape EAST_AABB;
    //standard : DIRECTION_AABB_OS(D2)
    //DIRECTION : on what face of the block the glass part of the windows is
    //OSE : Open Small East
    //OSN : Open Small North
    //OSW : Open Small West
    //OSS : Open Small South
    protected static final VoxelShape SOUTH_AABB_OSE;
    protected static final VoxelShape NORTH_AABB_OSE;
    protected static final VoxelShape WEST_AABB_OSN;
    protected static final VoxelShape EAST_AABB_OSN;

    protected static final VoxelShape SOUTH_AABB_OSW;
    protected static final VoxelShape NORTH_AABB_OSW;
    protected static final VoxelShape WEST_AABB_OSS;
    protected static final VoxelShape EAST_AABB_OSS;


    public WindowBlock(String key) {
        super(Properties.of(Material.GLASS)
                .lightLevel(value -> 0)
                .strength(3.0f)
                .sound(SoundType.GLASS)
                .noOcclusion()
        );
        this.setRegistryName(key);
        this.registerDefaultState(this.getStateDefinition().any().setValue(ROTATED, Boolean.FALSE).setValue(WINDOW_PLACE,WindowPlace.FULL));
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        boolean isRotated = state.getValue(ROTATED);
        if (!isRotated) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean isOpen = state.getValue(BlockStateProperties.OPEN);
            WindowPlace place = state.getValue(WINDOW_PLACE);
            boolean isLeft = place.isLeftPlace();
            boolean isRight = place.isRightPlace();
            switch (facing) {
                case EAST:
                default:
                    if (!isOpen) {
                        return EAST_AABB;
                    }
                    if (isLeft) {
                        return NORTH_AABB;
                    }
                    if (isRight) {
                        return SOUTH_AABB;
                    }
                    return Shapes.or(NORTH_AABB_OSE, SOUTH_AABB_OSE);
                case SOUTH:
                    if (!isOpen) {
                        return SOUTH_AABB;
                    }
                    if (isLeft) {
                        return EAST_AABB;
                    }
                    if (isRight) {
                        return WEST_AABB;
                    }
                    return Shapes.or(EAST_AABB_OSS, WEST_AABB_OSS);
                case WEST:
                    if (!isOpen) {
                        return WEST_AABB;
                    }
                    if (isLeft) {
                        return SOUTH_AABB;
                    }
                    if (isRight) {
                        return NORTH_AABB;
                    }
                    return Shapes.or(NORTH_AABB_OSW, SOUTH_AABB_OSW);
                case NORTH:
                    if (!isOpen) {
                        return NORTH_AABB;
                    }
                    if (isLeft) {
                        return WEST_AABB;
                    }
                    if (isRight) {
                        return EAST_AABB;
                    }
                    return Shapes.or(EAST_AABB_OSN, WEST_AABB_OSN);
            }
        }else {
            boolean isOpen = state.getValue(BlockStateProperties.OPEN);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            WindowPlace place = state.getValue(WINDOW_PLACE);
            return place.getVoxels(isOpen,facing);
        }
    }


    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        switch (type) {
            case LAND:
            case AIR:
                return state.getValue(BlockStateProperties.OPEN);
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WindowBlock)){
            return false;
        }
        String key = this.getDescriptionId();
        System.out.println("key : "+key);
        String other_key = ((WindowBlock)obj).getDescriptionId();
        System.out.println("other_key : "+other_key);
        return key.equals(other_key);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            ExtendDirection facing = Functions.getDirectionFromEntityAndNeighbor(entity,pos,world);
            WindowPlace placement = WindowPlace.getFromNeighboring(world,pos,state,facing);
            boolean isOpen = getOpenFromNeighbor(world,pos,placement,facing);
            world.setBlockAndUpdate(pos,state
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facing.getDirection())
                    .setValue(BlockStateProperties.OPEN,isOpen)
                    .setValue(WINDOW_PLACE,placement)
                    .setValue(ROTATED,facing.isRotated())
            );
            notifyNeighborBlock(placement,facing,world,pos,false);
        }
    }


    private boolean getOpenFromNeighbor(Level world, BlockPos pos, WindowPlace placement,ExtendDirection facing) {
        BlockPos neighborPos =placement.getRandNeighborPos(pos,facing);
        if (!(neighborPos == null)) {
            BlockState neighState = world.getBlockState(neighborPos);
            if (neighState.hasProperty(BlockStateProperties.OPEN)){
                return neighState.getValue(BlockStateProperties.OPEN);
            }
        }
        return false;
    }

    private void notifyNeighborBlock(WindowPlace placement,ExtendDirection facing,Level world,BlockPos pos,boolean openingStateOnly){
        List<WindowDirection> directions = placement.getDirectionOfChangingWindow(facing,world,pos);
        for (WindowDirection dir : directions){
            //the block position to offset
            BlockPos changingWindowPos = dir.offsetPos(pos);
            BlockState changingWindowState = world.getBlockState(changingWindowPos);
            Block changingWindow = changingWindowState.getBlock();
            if (this.equals(changingWindow)){
                WindowBlock window = (WindowBlock) changingWindow;
                if (openingStateOnly){
                    window.openOrCloseWindow(changingWindowState,changingWindowPos,world);
                }else {
                    window.updatePlacement(world,changingWindowPos,changingWindowState,facing);
                }
            }
        }
    }

    public void openOrCloseWindow(BlockState state,BlockPos pos,Level world){
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        world.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.OPEN,!isOpen));

    }


    //1.15 function
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isRotated = state.getValue(ROTATED);
        ExtendDirection extDirection = ExtendDirection.getExtendedDirection(facing,isRotated);
        assert extDirection != null;
        if (isPlayerOutside(player,extDirection,pos)){
            return InteractionResult.FAIL;
        }
        openOrCloseWindow(state,pos,world);
        WindowPlace placement = state.getValue(WINDOW_PLACE);
        notifyNeighborBlock(placement,extDirection,world,pos,true);
        return InteractionResult.SUCCESS;
    }

    private boolean isPlayerOutside(Player player, ExtendDirection direction, BlockPos pos){
        Vec3 relPlayerPos = player.position().subtract(new Vec3(pos.getX()+(direction.getAxis() == ExtendDirection.Axis.XMZ ? 1 : 0) ,pos.getY(),pos.getZ()));
        double playerPosXZ = direction.getAxis().project(relPlayerPos);
        // this series of test is blocking the opening of windows from outside
        if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            if (playerPosXZ > 0) {
                return true;
            }
        }else {
            if (playerPosXZ < 0){
                return true;
            }
        }
        return false;
    }


    private void updatePlacement(Level world, BlockPos pos, BlockState state, ExtendDirection facing) {
        WindowPlace placement = WindowPlace.getFromNeighboring(world,pos,state,facing);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        world.setBlockAndUpdate(pos,state.setValue(WINDOW_PLACE,placement)
                //we need to have same facing for all block that are in valid position
                .setValue(BlockStateProperties.HORIZONTAL_FACING,facing.getDirection())
                .setValue(BlockStateProperties.OPEN,isOpen)
                .setValue(ROTATED,facing.isRotated())
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN, WINDOW_PLACE, ROTATED);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void playerDestroy(Level world, Player entity, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        WindowPlace wp = state.getValue(WINDOW_PLACE);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isRotated = state.getValue(ROTATED);
        ExtendDirection extDir = ExtendDirection.getExtendedDirection(facing,isRotated);
        super.playerWillDestroy(world,pos,state,player);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(),35);
        ItemStack stack = player.getMainHandItem();
        if (!world.isClientSide && !player.isCreative()) {
            Block.dropResources(state, world, pos, null, player, stack);
        }
        notifyNeighborBlock(wp,extDir,world,pos,false);
    }

    static {
        WINDOW_PLACE = EnumProperty.create("window_place",WindowPlace.class);
        ROTATED = BooleanProperty.create("rotated");
        SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
        NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
        WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

        SOUTH_AABB_OSE = Block.box(1.0D, 2.0D, 2.0D, 7.0D, 14.0D, 3.0D);
        NORTH_AABB_OSE = Block.box(1.0D, 2.0D, 13.0D, 7.0D, 14.0D, 14.0D);
        WEST_AABB_OSS =  Block.box(13.0D, 2.0D, 1.0D, 14.0D, 14.0D, 7.0D);
        EAST_AABB_OSS =  Block.box(2.0D, 2.0D, 1.0D, 3.0D, 14.0D, 7.0D);

        SOUTH_AABB_OSW = Block.box(9.0D, 2.0D, 2.0D, 15.0D, 14.0D, 3.0D);
        NORTH_AABB_OSW = Block.box(9.0D, 2.0D, 13.0D, 15.0D, 14.0D, 14.0D);
        WEST_AABB_OSN =  Block.box(13.0D, 2.0D, 9.0D, 14.0D, 14.0D, 15.0D);
        EAST_AABB_OSN =  Block.box(2.0D, 2.0D, 9.0D, 3.0D, 14.0D, 15.0D);
    }
}
