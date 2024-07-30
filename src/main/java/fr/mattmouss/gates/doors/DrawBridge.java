package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.enum_door.DrawBridgePosition;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.tileentity.DrawBridgeTileEntity;
import fr.mattmouss.gates.util.Functions;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import fr.mattmouss.gates.voxels.VoxelDoubles;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;


public class DrawBridge extends Block implements EntityBlock {
    public static EnumProperty<DrawBridgePosition> POSITION=EnumProperty.create("position",DrawBridgePosition.class);

    public static IntegerProperty ANIMATION = IntegerProperty.create("animation",0,4);

    public DrawBridge(String key) {
        super(Properties.of(Material.METAL)
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        );
        this.setRegistryName(key);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DrawBridgeTileEntity(blockPos,blockState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        DrawBridgePosition position = state.getValue(POSITION);
        int animState=state.getValue(ANIMATION);
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        //opening bridge part when closed (or almost)
        if (position.isBridge()) {
            if (animState != 4 || position.isBridgeExt())return Shapes.empty();
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
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null){
            Direction facing = Functions.getDirectionFromEntity(placer,pos).getOpposite();
            for (DrawBridgePosition position : DrawBridgePosition.values()){
                worldIn.setBlockAndUpdate(position.getOffsetPos(pos,facing),state.setValue(POSITION,position));
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (checkFeasibility(context)){
            BlockState state = defaultBlockState();
            Direction facing = context.getHorizontalDirection();
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facing).setValue(POSITION, DrawBridgePosition.DOOR_LEFT_DOWN).setValue(ANIMATION,0).setValue(BlockStateProperties.POWERED,false);
        } else {
            return null;
        }
    }

    public boolean checkFeasibility(BlockPlaceContext context){
        BlockPos pos=context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        Level world= context.getLevel();
        List<BlockPos> posList = getNeighborPositions(facing,pos,DrawBridgePosition.DOOR_LEFT_DOWN);
        for (BlockPos aPos : posList){
            if (!world.getBlockState(aPos).canBeReplaced(context)){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
    public void playerDestroy(Level world, Player entity, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, entity, pos, Blocks.AIR.defaultBlockState(), tileEntity, stack);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player entity) {
        System.out.println("destroying all block of turn stile");
        ItemStack stack = entity.getMainHandItem();
        if (!world.isClientSide) {
            Block.dropResources(state, world, pos, null, entity, stack);
        }
        super.playerWillDestroy(world, pos, state, entity);
    }

    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.getValue(ANIMATION)==4);
            default:
                return false;
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
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
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean flag = isNeighBorDoorBlockPowered(pos,state,worldIn);
        DrawBridgePosition position = state.getValue(POSITION);
        if (blockIn != this && flag != state.getValue(BlockStateProperties.POWERED) && position == DrawBridgePosition.DOOR_LEFT_DOWN){
            worldIn.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.POWERED,flag));
        }
    }

    private boolean isNeighBorDoorBlockPowered(BlockPos pos, BlockState state, Level world) {
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (type == ModBlock.DRAW_BRIDGE_TILE_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof DrawBridgeTileEntity) {
                ((DrawBridgeTileEntity) t).tick(blockState);
            }
        })) : null;
    }
}
