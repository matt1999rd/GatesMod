package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DrawBridgePosition;
import fr.mattmouss.gates.tileentity.DrawBridgeTileEntity;
import fr.mattmouss.gates.util.Functions;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import fr.mattmouss.gates.voxels.VoxelDoubles;
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
//import net.minecraft.util.BlockRenderLayer;
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

    public DrawBridge(String key) {
        super(Properties.of(Material.METAL)
                .strength(2.0f)
                .sound(SoundType.METAL)
                //1.15 function
                //.notSolid()
        );
        this.setRegistryName(key);
    }

/*
    //1.14.4 function replaced by notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
*/


    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(POSITION) == DrawBridgePosition.DOOR_LEFT_DOWN;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return hasTileEntity(state)?new DrawBridgeTileEntity():null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        DrawBridgePosition position = state.getValue(POSITION);
        int animState=state.getValue(ANIMATION);
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        //opening bridge part when closed (or almost)
        if (position.isBridge()) {
            if (animState != 4 || position.isBridgeExt())return VoxelShapes.empty();
            int begX = (position.isRight())?4:0;
            VoxelDoubles voxel = new VoxelDoubles(begX,0,16,12,2,28,true);
            return voxel.rotate(Direction.SOUTH,facing).getAssociatedShape();
        }
        return getSpecialShape(position,animState,facing);
    }

    private VoxelShape getSpecialShape(DrawBridgePosition position,int animState,Direction facing){
        int meta=position.getMeta();
        int facingIndex=facing.get2DDataValue();
        int index=20*meta+4*animState+facingIndex;
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.drawBridgeShape[index];
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            Direction facing = Functions.getDirectionFromEntity(placer,pos).getOpposite();
            for (DrawBridgePosition position : DrawBridgePosition.values()){
                worldIn.setBlockAndUpdate(position.getOffsetPos(pos,facing),state.setValue(POSITION,position));
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (checkFeasibility(context)){
            BlockState state = defaultBlockState();
            Direction facing = context.getHorizontalDirection();
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facing).setValue(POSITION, DrawBridgePosition.DOOR_LEFT_DOWN).setValue(ANIMATION,0).setValue(BlockStateProperties.POWERED,false);
        } else {
            return null;
        }
    }

    public boolean checkFeasibility(BlockItemUseContext context){
        BlockPos pos=context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        World world= context.getLevel();
        List<BlockPos> posList = getNeighborPositions(facing,pos,DrawBridgePosition.DOOR_LEFT_DOWN);
        for (BlockPos aPos : posList){
            if (!world.getBlockState(aPos).canBeReplaced(context)){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
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
    public void playerDestroy(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of turn stile");
        ItemStack stack = entity.getMainHandItem();
        if (!world.isClientSide) {
            Block.dropResources(state, world, pos, null, entity, stack);
        }
        super.playerWillDestroy(world, pos, state, entity);
    }

    public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.getValue(ANIMATION)==4);
            default:
                return false;
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        DrawBridgePosition position = stateIn.getValue(POSITION);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing) &&  !(facingState.getBlock() instanceof DrawBridge)){
            return Blocks.AIR.defaultBlockState();
        }
        if (facing == Direction.DOWN && !facingState.getMaterial().blocksMotion() && !stateIn.getValue(POSITION).isBridge()){
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }

    private boolean isInnerUpdate(DrawBridgePosition position, Direction facingUpdate, Direction blockFacing){
        return position.isInnerUpdate(facingUpdate, blockFacing);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        DrawBridgePosition position = state.getValue(POSITION);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED) && position == DrawBridgePosition.DOOR_LEFT_DOWN){
            worldIn.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.POWERED,flag));
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, World world) {
        DrawBridgePosition dbp = state.getValue(POSITION);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        List<BlockPos> blockPosList = getNeighborPositions(facing,pos,dbp);
        if (world.hasNeighborSignal(pos)){
            return true;
        }
        for (BlockPos neiPos : blockPosList){
            if (world.hasNeighborSignal(neiPos)){
                return true;
            }
        }
        return false;
    }


}
