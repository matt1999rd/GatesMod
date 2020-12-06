package fr.mattmouss.gates.windows;

import fr.mattmouss.gates.tools.VoxelInts;
import fr.mattmouss.gates.util.ExtendDirection;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;



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
        super(Properties.create(Material.GLASS)
                .lightValue(0)
                .hardnessAndResistance(3.0f)
                .sound(SoundType.GLASS)
                //1.15 function
                //.notSolid()
        );
        this.setRegistryName(key);
        this.setDefaultState(this.getStateContainer().getBaseState().with(ROTATED, Boolean.FALSE).with(WINDOW_PLACE,WindowPlace.FULL));
    }





    //1.14 notSolid() function

    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        boolean isRotated = state.get(ROTATED);
        if (!isRotated) {
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            boolean isOpen = state.get(BlockStateProperties.OPEN);
            WindowPlace place = state.get(WINDOW_PLACE);
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
                    return VoxelShapes.or(NORTH_AABB_OSE, SOUTH_AABB_OSE);
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
                    return VoxelShapes.or(EAST_AABB_OSS, WEST_AABB_OSS);
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
                    return VoxelShapes.or(NORTH_AABB_OSW, SOUTH_AABB_OSW);
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
                    return VoxelShapes.or(EAST_AABB_OSN, WEST_AABB_OSN);
            }
        }else {
            boolean isOpen = state.get(BlockStateProperties.OPEN);
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            WindowPlace place = state.get(WINDOW_PLACE);
            VoxelShape usedVoxels = place.getVoxels(isOpen,facing);
            return usedVoxels;
        }
    }


    public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
        switch (type) {
            case LAND:
            case AIR:
                return state.get(BlockStateProperties.OPEN);
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WindowBlock)){
            return false;
        }
        String key = this.getTranslationKey();
        System.out.println("key : "+key);
        String other_key = ((WindowBlock)obj).getTranslationKey();
        System.out.println("other_key : "+other_key);
        return key.equals(other_key);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            ExtendDirection facing = Functions.getDirectionFromEntityAndNeighbor(entity,pos,world);
            WindowPlace placement = WindowPlace.getFromNeighboring(world,pos,state,facing);
            boolean isOpen = getOpenFromNeighbor(world,pos,placement,facing);
            world.setBlockState(pos,state
                    .with(BlockStateProperties.HORIZONTAL_FACING, facing.getDirection())
                    .with(BlockStateProperties.OPEN,isOpen)
                    .with(WINDOW_PLACE,placement)
                    .with(ROTATED,facing.isRotated())
            );
            notifyNeighborBlock(placement,facing,world,pos,false);
        }
    }


    private boolean getOpenFromNeighbor(World world, BlockPos pos, WindowPlace placement,ExtendDirection facing) {
        BlockPos neighborPos =placement.getRandNeighborPos(pos,facing);
        if (!(neighborPos == null)) {
            BlockState neighState = world.getBlockState(neighborPos);
            if (neighState.has(BlockStateProperties.OPEN)){
                return neighState.get(BlockStateProperties.OPEN);
            }
        }
        return false;
    }

    private void notifyNeighborBlock(WindowPlace placement,ExtendDirection facing,World world,BlockPos pos,boolean openingStateOnly){
        List<WindowDirection> directions = placement.getDirectionOfChangingWindow(facing,world,pos);
        for (WindowDirection dir : directions){
            //the blockpos to offset
            BlockPos ch_block_pos = pos;
            //get nb_offset int[]
            int[] offsets = dir.getDirections();
            //when dir value is 0 this means that all offset has been taken in account
            for(int i=0;i<10;i++){
                if (offsets[i] != 0){
                    ch_block_pos = ExtendDirection.byIndex(i).offset(ch_block_pos,offsets[i]);
                }
            }
            Block ch_window = world.getBlockState(ch_block_pos).getBlock();
            BlockState ch_w_state = world.getBlockState(ch_block_pos);
            if (this.equals(ch_window)){
                WindowBlock window = (WindowBlock)ch_window;
                if (openingStateOnly) window.openOrCloseWindow(ch_w_state,ch_block_pos,world);
                else window.updatePlacement(world,ch_block_pos,ch_w_state,facing);
            }
        }
    }

    public void openOrCloseWindow(BlockState state,BlockPos pos,World world){
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        world.setBlockState(pos,state.with(BlockStateProperties.OPEN,!isOpen));

    }

    /*
    //1.15 function
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (player != null){
            openOrCloseWindow(state,pos,world);
            WindowPlace placement = state.get(WINDOW_PLACE);
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            boolean isRotated = state.get(ROTATED);
            ExtendDirection extDir = ExtendDirection.getExtendedDirection(facing,isRotated);
            notifyNeighborBlock(placement,extDir,world,pos,true);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

     */




    //1.14 function onBlockActivated
    @Override
    public boolean func_220051_a(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (player != null){
            openOrCloseWindow(state,pos,world);
            WindowPlace placement = state.get(WINDOW_PLACE);
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            boolean isRotated = state.get(ROTATED);
            ExtendDirection extDir = ExtendDirection.getExtendedDirection(facing,isRotated);
            notifyNeighborBlock(placement,extDir,world,pos,true);
            return true;
        }
        return false;
    }



    private void updatePlacement(World world, BlockPos pos, BlockState state, ExtendDirection facing) {
        WindowPlace placement = WindowPlace.getFromNeighboring(world,pos,state,facing);
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        world.setBlockState(pos,state.with(WINDOW_PLACE,placement)
                //we need to have same facing for all block that are in valid position
                .with(BlockStateProperties.HORIZONTAL_FACING,facing.getDirection())
                .with(BlockStateProperties.OPEN,isOpen)
                .with(ROTATED,facing.isRotated())
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN, WINDOW_PLACE, ROTATED);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void harvestBlock(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.harvestBlock(world, entity, pos, Blocks.AIR.getDefaultState(), tileEntity, stack);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        WindowPlace wp = state.get(WINDOW_PLACE);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        boolean isRotated = state.get(ROTATED);
        ExtendDirection extDir = ExtendDirection.getExtendedDirection(facing,isRotated);
        super.onBlockHarvested(world,pos,state,player);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(),35);
        ItemStack stack = player.getHeldItemMainhand();
        if (!world.isRemote && !player.isCreative() && player.canHarvestBlock(state)) {
            Block.spawnDrops(state, world, pos, null, player, stack);
        }
        notifyNeighborBlock(wp,extDir,world,pos,false);
    }

    static {
        WINDOW_PLACE = EnumProperty.create("window_place",WindowPlace.class);
        ROTATED = BooleanProperty.create("rotated");
        SOUTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
        NORTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
        WEST_AABB = Block.makeCuboidShape(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        EAST_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

        SOUTH_AABB_OSE = Block.makeCuboidShape(1.0D, 2.0D, 2.0D, 7.0D, 14.0D, 3.0D);
        NORTH_AABB_OSE = Block.makeCuboidShape(1.0D, 2.0D, 13.0D, 7.0D, 14.0D, 14.0D);
        WEST_AABB_OSS =  Block.makeCuboidShape(13.0D, 2.0D, 1.0D, 14.0D, 14.0D, 7.0D);
        EAST_AABB_OSS =  Block.makeCuboidShape(2.0D, 2.0D, 1.0D, 3.0D, 14.0D, 7.0D);

        SOUTH_AABB_OSW = Block.makeCuboidShape(9.0D, 2.0D, 2.0D, 15.0D, 14.0D, 3.0D);
        NORTH_AABB_OSW = Block.makeCuboidShape(9.0D, 2.0D, 13.0D, 15.0D, 14.0D, 14.0D);
        WEST_AABB_OSN =  Block.makeCuboidShape(13.0D, 2.0D, 9.0D, 14.0D, 14.0D, 15.0D);
        EAST_AABB_OSN =  Block.makeCuboidShape(2.0D, 2.0D, 9.0D, 3.0D, 14.0D, 15.0D);
    }
}
