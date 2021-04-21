package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DrawBridgePosition;
import fr.mattmouss.gates.tileentity.DrawBridgeTileEntity;
import fr.mattmouss.gates.util.Functions;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
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
import java.util.List;


public class DrawBridge extends Block {
    public static EnumProperty<DrawBridgePosition> POSITION=EnumProperty.create("position",DrawBridgePosition.class);

    public static IntegerProperty ANIMATION = IntegerProperty.create("animation",0,4);

    public DrawBridge() {
        super(Properties.create(Material.IRON)
                .hardnessAndResistance(2.0f)
                .sound(SoundType.METAL)
                //1.15 function
                //.notSolid()
        );
        this.setRegistryName("draw_bridge");
    }

    //1.14.4 function replaced by notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(POSITION) == DrawBridgePosition.DOOR_LEFT_DOWN;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return hasTileEntity(state)?new DrawBridgeTileEntity():null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        DrawBridgePosition position = state.get(POSITION);
        int animState=state.get(ANIMATION);
        Direction facing=state.get(BlockStateProperties.HORIZONTAL_FACING);
        //opening bridge part when closed (or almost)
        if (position.isBridge()) {
            return VoxelShapes.empty();
        }
        return getSpecialShape(position,animState,facing);
    }

    private VoxelShape getSpecialShape(DrawBridgePosition position,int animState,Direction facing){
        int meta=position.getMeta();
        int facingIndex=facing.getHorizontalIndex();
        int index=4*meta+5*animState+facingIndex;
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.drawBridgeShape[index];
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            Direction facing = Functions.getDirectionFromEntity(placer,pos).getOpposite();
            for (DrawBridgePosition position : DrawBridgePosition.values()){
                worldIn.setBlockState(position.getOffsetPos(pos,facing),state.with(POSITION,position));
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (checkFeasibility(context)){
            BlockState state = getDefaultState();
            Direction facing = context.getPlacementHorizontalFacing();
            return state.with(BlockStateProperties.HORIZONTAL_FACING,facing).with(POSITION, DrawBridgePosition.DOOR_LEFT_DOWN).with(ANIMATION,0).with(BlockStateProperties.POWERED,false);
        } else {
            return null;
        }
    }

    public boolean checkFeasibility(BlockItemUseContext context){
        BlockPos pos=context.getPos();
        Direction facing = context.getPlacementHorizontalFacing();
        World world= context.getWorld();
        List<BlockPos> posList = getNeighborPositions(facing,pos,DrawBridgePosition.DOOR_LEFT_DOWN);
        for (BlockPos aPos : posList){
            if (!world.getBlockState(aPos).isReplaceable(context)){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED,BlockStateProperties.HORIZONTAL_FACING,POSITION,ANIMATION);
    }

    public List<BlockPos> getNeighborPositions(Direction facing,BlockPos pos,DrawBridgePosition position){
        List<BlockPos> posList = Lists.newArrayList();
        BlockPos leftDownPos = position.getCounterOffsetPos(pos,facing);
        for (DrawBridgePosition position1 : DrawBridgePosition.values()){
            posList.add(position1.getOffsetPos(leftDownPos,facing));
        }
        return posList;
    }

    @Override
    public void harvestBlock(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.harvestBlock(world, entity, pos, Blocks.AIR.getDefaultState(), tileEntity, stack);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of turn stile");
        ItemStack stack = entity.getHeldItemMainhand();
        if (!world.isRemote) {
            Block.spawnDrops(state, world, pos, null, entity, stack);
        }
        super.onBlockHarvested(world, pos, state, entity);
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
        DrawBridgePosition position = stateIn.get(POSITION);
        Direction blockFacing = stateIn.get(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing) &&  !(facingState.getBlock() instanceof DrawBridge)){
            return Blocks.AIR.getDefaultState();
        }
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMovement()){
            return Blocks.AIR.getDefaultState();
        }
        return stateIn;
    }

    private boolean isInnerUpdate(DrawBridgePosition position, Direction facingUpdate, Direction blockFacing){
        return position.isInnerUpdate(facingUpdate, blockFacing);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        DrawBridgePosition position = state.get(POSITION);
        if (blockIn != this && flag != state.get(BlockStateProperties.POWERED) && position == DrawBridgePosition.DOOR_LEFT_DOWN){
            worldIn.setBlockState(pos,state.with(BlockStateProperties.POWERED,flag));
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, World world) {
        DrawBridgePosition dbp = state.get(POSITION);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        List<BlockPos> blockPosList = getNeighborPositions(facing,pos,dbp);
        if (world.isBlockPowered(pos)){
            return true;
        }
        for (BlockPos neiPos : blockPosList){
            if (world.isBlockPowered(neiPos)){
                return true;
            }
        }
        return false;
    }


}
