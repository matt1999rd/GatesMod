package fr.mattmouss.gates.windows;

import com.mojang.datafixers.types.Func;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

//TODO : change the sound

public class WindowBlock extends Block {

    public static EnumProperty<WindowPlace> WINDOW_PLACE;

    protected static final VoxelShape SOUTH_AABB;
    protected static final VoxelShape NORTH_AABB;
    protected static final VoxelShape WEST_AABB;
    protected static final VoxelShape EAST_AABB;


    public WindowBlock(String key) {
        super(Properties.create(Material.GLASS)
                .lightValue(0)
                .hardnessAndResistance(3.0f)
                .sound(SoundType.GLASS)
                //1.15 function
                //.notSolid()
        );
        this.setRegistryName(key);
    }

    //todo : add the function for 1.14.4 to replace notSolid


    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        Direction lvt_5_1_ = state.get(BlockStateProperties.HORIZONTAL_FACING);
        switch (lvt_5_1_) {
            case EAST:
            default:
                return EAST_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case WEST:
                return WEST_AABB;
            case NORTH:
                return NORTH_AABB;
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
            Direction facing = Functions.getDirectionFromEntity(entity,pos);
            WindowPlace placement = WindowPlace.getFromNeighboring(world,pos,state,facing);
            world.setBlockState(pos,state
                    .with(BlockStateProperties.HORIZONTAL_FACING, Functions.getDirectionFromEntity(entity,pos))
                    .with(BlockStateProperties.OPEN,false)
                    .with(WINDOW_PLACE,placement)
            );
            notifyNeighborBlock(placement,facing,world,pos);
        }
    }

    private void notifyNeighborBlock(WindowPlace placement,Direction facing,World world,BlockPos pos){
        List<WindowDirection> directions = placement.getDirectionOfChangingWindow(facing,world,pos);
        for (WindowDirection dir : directions){
            //the blockpos to offset
            BlockPos ch_block_pos = pos;
            //get nb_offset int[]
            int[] offsets = dir.getDirections();
            //when dir value is 0 this means that all offset has been taken in account
            for(int i=0;i<6;i++){
                if (offsets[i] != 0){
                    ch_block_pos = ch_block_pos.offset(Direction.byIndex(i),offsets[i]);
                }
            }
            Block ch_window = world.getBlockState(ch_block_pos).getBlock();
            BlockState ch_w_state = world.getBlockState(ch_block_pos);
            if (this.equals(ch_window)){
                ((WindowBlock)ch_window).updatePlacement(world,ch_block_pos,ch_w_state,facing);
            }
        }
    }

    public void openOrCloseWindow(BlockState state,BlockPos pos,World world){
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        world.setBlockState(pos,state.with(BlockStateProperties.OPEN,!isOpen));

    }

    //1.15 function
    /*
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (player != null){
            openOrCloseWindow(state,pos,world);
            WindowPlace placement = state.get(WINDOW_PLACE);
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            List<WindowDirection> directions = placement.getDirectionOfChangingWindow(facing,world,pos);
            for (WindowDirection dir : directions){
                //the blockpos to offset
                BlockPos ch_block_pos = pos;
                //get nb_offset int[]
                int[] offsets = dir.getDirections();
                //when dir value is 0 this means that all offset has been taken in account
                for(int i=0;i<6;i++){
                    if (offsets[i] != 0){
                        ch_block_pos = ch_block_pos.offset(Direction.byIndex(i),offsets[i]);
                    }
                }
                Block ch_window = world.getBlockState(ch_block_pos).getBlock();
                BlockState ch_w_state = world.getBlockState(ch_block_pos);
                if (this.equals(ch_window)){
                    ((WindowBlock)ch_window).openOrCloseWindow(ch_w_state,ch_block_pos,world);
                }
            }
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
            List<WindowDirection> directions = placement.getDirectionOfChangingWindow(facing,world,pos);
            for (WindowDirection dir : directions){
                //the blockpos to offset
                BlockPos ch_block_pos = pos;
                //get nb_offset int[]
                int[] offsets = dir.getDirections();
                //when dir value is 0 this means that all offset has been taken in account
                for(int i=0;i<6;i++){
                    if (offsets[i] != 0){
                        ch_block_pos = ch_block_pos.offset(Direction.byIndex(i),offsets[i]);
                    }
                }
                Block ch_window = world.getBlockState(ch_block_pos).getBlock();
                BlockState ch_w_state = world.getBlockState(ch_block_pos);
                if (this.equals(ch_window)){
                    ((WindowBlock)ch_window).openOrCloseWindow(ch_w_state,ch_block_pos,world);
                }
            }
            return true;
        }
        return false;
    }

    private void updatePlacement(World world, BlockPos pos, BlockState state, Direction facing) {
        WindowPlace placement = WindowPlace.getFromNeighboring(world,pos,state,null);
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        world.setBlockState(pos,state.with(WINDOW_PLACE,placement)
                //we need to have same facing for all block that is in valid position
                .with(BlockStateProperties.HORIZONTAL_FACING,facing)
                .with(BlockStateProperties.OPEN,isOpen)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN, WINDOW_PLACE);
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
        super.onBlockHarvested(world,pos,state,player);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(),35);
        ItemStack stack = player.getHeldItemMainhand();
        if (!world.isRemote && !player.isCreative() && player.canHarvestBlock(state)) {
            Block.spawnDrops(state, world, pos, null, player, stack);
        }
        notifyNeighborBlock(wp,facing,world,pos);
    }

    static {
        WINDOW_PLACE = EnumProperty.create("window_place",WindowPlace.class);
        SOUTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
        NORTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
        WEST_AABB = Block.makeCuboidShape(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        EAST_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    }
}
