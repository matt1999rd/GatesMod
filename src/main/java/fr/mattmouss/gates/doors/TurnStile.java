package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.energystorage.IdTracker;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.items.TollKeyItem;
import fr.mattmouss.gates.items.TurnStileKeyItem;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.SetIdPacket;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import fr.mattmouss.gates.voxels.VoxelInts;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
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
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.mattmouss.gates.enum_door.TurnSPosition.*;

public class TurnStile extends Block {

    public static EnumProperty<TurnSPosition> TS_POSITION ;
    public static IntegerProperty ANIMATION;
    public static BooleanProperty WAY_IS_ON;


    static  {
        TS_POSITION = EnumProperty.create("ts_position",TurnSPosition.class);
        ANIMATION = IntegerProperty.create("animation",0,1);
        WAY_IS_ON = BooleanProperty.create("way_is_on");
    }

    public TurnStile() {
        super(Properties.create(Material.IRON)
                .hardnessAndResistance(2.0f)
                .sound(SoundType.METAL)
                //1.15 function
                //.notSolid()
        );
        this.setRegistryName("turn_stile");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        if (state.get(TS_POSITION)!=TurnSPosition.MAIN && state.get(TS_POSITION)!=TurnSPosition.UP_BLOCK){
            //we need to block the jumping of player on the turn stile which mean fraud.
            return Block.makeCuboidShape(0,0,0,16,18,16);
        }else if (state.get(TS_POSITION) == TurnSPosition.UP_BLOCK){
            //we had this block because player is jumping on the turn stile without this block
            return (!state.get(WAY_IS_ON))? VoxelShapes.fullCube() : VoxelShapes.empty();
        }

        return getTurnStileShape(state);
    }

    //1.14.4 function replaced by notSolid()
    @Override
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }


    private VoxelShape getTurnStileShape(BlockState state){
        int anim = state.get(ANIMATION);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        VoxelShape shape;
        VoxelInts RotationBlock;
        VoxelInts[] ForwardPart,BackwardPart,MiddlePart;
        RotationBlock = new VoxelInts(0,7,6,2,10,10,false);

        if (anim == 0){
            MiddlePart = new VoxelInts[]{
                    new VoxelInts(2,10,7.4676,2,1,1,true),
                    new VoxelInts(4,11,7.4676,2,1,1,true),
                    new VoxelInts(6,12,7.4676,2,1,1,true),
                    new VoxelInts(8,13,7.4676,1,1,1,true),
                    new VoxelInts(9,14,7.4676,2,1,1,true),
                    new VoxelInts(11,15,7.4676,2,1,1,true)
            };
            ForwardPart = new VoxelInts[]{
                    new VoxelInts(3,7,6.5,1,1,1,true),
                    new VoxelInts(4,6,5.5,1,1,1,true),
                    new VoxelInts(5,5,4.5,2,1,1,true),
                    new VoxelInts(7,5,3.5,1,1,1,true),
                    new VoxelInts(8,4,2.5,1,1,1,true),
                    new VoxelInts(9,3,1.5,1,1,1,true),
                    new VoxelInts(10,3,0.5,1,1,1,true)
            };

            BackwardPart = new VoxelInts[]{
                    new VoxelInts(3,7,8.5,1,1,1,true),
                    new VoxelInts(4,6,9.5,1,1,1,true),
                    new VoxelInts(5,5,10.5,2,1,1,true),
                    new VoxelInts(7,5,11.5,1,1,1,true),
                    new VoxelInts(8,4,12.5,1,1,1,true),
                    new VoxelInts(9,3,13.5,1,1,1,true),
                    new VoxelInts(10,3,14.5,1,1,1,true)
            };
        }else if (anim == 1){
            MiddlePart = new VoxelInts[]{
                    new VoxelInts(1,6,8,2,1,1,true),
                    new VoxelInts(3,5,8,1,1,1,true),
                    new VoxelInts(4,4,8,2,1,1,true),
                    new VoxelInts(6,3,8,2,1,1,true),
                    new VoxelInts(8,2,8,2,1,1,true),
                    new VoxelInts(10,1,8,1,1,1,true),
                    new VoxelInts(11,0,8,2,1,1,true)
            };
            ForwardPart = new VoxelInts[]{
                    new VoxelInts(2,10.5,6,1,1,1,true),
                    new VoxelInts(3,11,5.5,1,1,1,true),
                    new VoxelInts(4,11.8,4.5,1,1,1,true),
                    new VoxelInts(5,12.2,4,1,1,1,true),
                    new VoxelInts(6,12.8,3,1,1,1,true),
                    new VoxelInts(7,13.4,2,1,1,1,true),
                    new VoxelInts(8,14,1.5,1,1,1,true),
                    new VoxelInts(9,14.5,0.5,1,1,1,true)
            };
            BackwardPart = new VoxelInts[]{
                    new VoxelInts(2,10.5,9,1,1,1,true),
                    new VoxelInts(3,11,9.5,1,1,1,true),
                    new VoxelInts(4,11.8,10.5,1,1,1,true),
                    new VoxelInts(5,12.2,11,1,1,1,true),
                    new VoxelInts(6,12.8,12,1,1,1,true),
                    new VoxelInts(7,13.4,13,1,1,1,true),
                    new VoxelInts(8,14,13.5,1,1,1,true),
                    new VoxelInts(9,14.5,14.5,1,1,1,true)
            };

        }else {
            ForwardPart = new VoxelInts[]{VoxelInts.EMPTY} ;
            BackwardPart = new VoxelInts[]{VoxelInts.EMPTY};
            MiddlePart = new VoxelInts[]{VoxelInts.EMPTY};
        }

        shape = RotationBlock.rotate(Direction.NORTH,facing).getAssociatedShape();

        for (VoxelInts vi : ForwardPart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }
        for (VoxelInts vi : BackwardPart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }
        for (VoxelInts vi : MiddlePart){
            vi =vi.rotate(Direction.NORTH,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }

        return shape;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TurnStileTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        if (player != null){
            Direction direction = Functions.getDirectionFromEntity(player,pos);
            DoorHingeSide dhs = Functions.getHingeSideFromEntity(player,pos,direction);
            if (!world.isRemote) {
                //we change the id for the block Control Unit where the tech gui will open
                TurnStileTileEntity tste = (TurnStileTileEntity) world.getTileEntity(pos);
                tste.changeId();
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayerEntity)player),new SetIdPacket(pos,tste.getId()));
            }
            //boolean that return true when Control Unit is on the right
            boolean CUisOnRight = (dhs == DoorHingeSide.RIGHT);
            //the control unit block (left if DHS.left and right if DHS.right)
            TurnSPosition tsp = (CUisOnRight) ? TurnSPosition.RIGHT_BLOCK : TurnSPosition.LEFT_BLOCK;
            world.setBlockState(pos,
                    state
                    .with(BlockStateProperties.HORIZONTAL_FACING,direction)
                    .with(BlockStateProperties.DOOR_HINGE,dhs)
                    .with(ANIMATION,0)
                    .with(TS_POSITION,tsp)
                    .with(WAY_IS_ON,false)
            );
            //the main block
            //offset with rotateY is for left block and rotateYCCW is for right block regarding direction of facing
            BlockPos MainPos = (CUisOnRight) ? pos.offset(direction.rotateY()): pos.offset(direction.rotateYCCW());
            world.setBlockState(MainPos,
                    state
                    .with(BlockStateProperties.HORIZONTAL_FACING,direction)
                    .with(BlockStateProperties.DOOR_HINGE,dhs)
                    .with(ANIMATION,0)
                    .with(TS_POSITION,TurnSPosition.MAIN)
                    .with(WAY_IS_ON,false)
            );
            //the up block
            world.setBlockState(MainPos.offset(Direction.UP),
                    state
                    .with(BlockStateProperties.HORIZONTAL_FACING,direction)
                    .with(BlockStateProperties.DOOR_HINGE,dhs)
                    .with(ANIMATION,0)
                    .with(TS_POSITION,TurnSPosition.UP_BLOCK)
                    .with(WAY_IS_ON,false)
            );

            //the right block
            BlockPos ExtremityPos = (CUisOnRight) ? pos.offset(direction.rotateY(),2) : pos.offset(direction.rotateYCCW(),2);
            TurnSPosition ExtremityTsp = (CUisOnRight) ? TurnSPosition.LEFT_BLOCK : TurnSPosition.RIGHT_BLOCK;
            world.setBlockState(ExtremityPos,
                    state
                    .with(BlockStateProperties.HORIZONTAL_FACING,direction)
                    .with(BlockStateProperties.DOOR_HINGE,dhs)
                    .with(ANIMATION,0)
                    .with(TS_POSITION,ExtremityTsp)
                    .with(WAY_IS_ON,false)
            );
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING,TS_POSITION,ANIMATION,BlockStateProperties.DOOR_HINGE,WAY_IS_ON);
    }

    @Override
    public void harvestBlock(World world, PlayerEntity entity, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.harvestBlock(world, entity, pos, Blocks.AIR.getDefaultState(), tileEntity, stack);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TurnSPosition position = stateIn.get(TS_POSITION);
        Direction blockFacing = stateIn.get(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing) &&  !(facingState.getBlock() instanceof TurnStile)){
            return Blocks.AIR.getDefaultState();
        }
        if (position.isSolid() && facing == Direction.DOWN && !facingState.getMaterial().blocksMovement()){
            if (!worldIn.isRemote())removeUselessKey(worldIn.getWorld(),currentPos,stateIn);
            return Blocks.AIR.getDefaultState();
        }
        return stateIn;
    }

    //block facing is the direction of forth block
    private boolean isInnerUpdate(TurnSPosition position, Direction facingUpdate, Direction blockFacing){
        return ( position == RIGHT_BLOCK && facingUpdate == blockFacing.rotateY()) ||
                (position == LEFT_BLOCK  && facingUpdate == blockFacing.rotateYCCW()) ||
                (position == MAIN && (facingUpdate.getAxis() == blockFacing.rotateY().getAxis() || facingUpdate == Direction.UP ) ) ||
                (position == UP_BLOCK && facingUpdate == Direction.DOWN);
    }

    private void removeUselessKey(World world,BlockPos pos,BlockState state){
        TurnStileKeyItem key = (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem();
        ItemStack oldStack = new ItemStack(key);
        BlockPos keyPos = getKeyPos(pos,state);
        key.setTSPosition(oldStack, world, keyPos);
        List<? extends PlayerEntity> players = world.getPlayers();
        AtomicBoolean foundKey = new AtomicBoolean(false);
        players.forEach(p -> {
            PlayerInventory inventory = p.inventory;
            if (!foundKey.get()) {
                if (inventory.hasItemStack(oldStack)) {
                    int slot = inventory.getSlotFor(oldStack);
                    inventory.mainInventory.set(slot, ItemStack.EMPTY);
                    foundKey.set(true);
                }
            }
        });
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of turn stile");
        if (!world.isRemote){
            removeUselessKey(world,pos,state);
        }
        ItemStack stack = entity.getHeldItemMainhand();
        if (!world.isRemote && !entity.isCreative()) {
            Block.spawnDrops(state, world, pos, null, entity, stack);
        }
    }

    private BlockPos getKeyPos(BlockPos pos, BlockState state) {
        TurnSPosition turnSPosition = state.get(TS_POSITION);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        switch (turnSPosition){
            case RIGHT_BLOCK:
                return pos.offset(facing.rotateY());
            case LEFT_BLOCK:
                return pos.offset(facing.rotateYCCW());
            case UP_BLOCK:
                return pos.down();
            case MAIN:
            default:
                return pos;
        }
    }

    public void deleteBlock(BlockPos pos, World world){
        TurnStileTileEntity tste = (TurnStileTileEntity) world.getTileEntity(pos);
        if (!world.isRemote && tste.isRightTSB()){
            IdTracker idTracker = world.getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(IdTracker::new,"idgates");
            idTracker.removeId(tste.getId());
        }
        world.setBlockState(pos,Blocks.AIR.getDefaultState(),35);
    }


    public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType pathType) {
        switch(pathType) {
            case LAND:
            case AIR:
                return (state.get(ANIMATION)==1);
            default:
                return false;
        }
    }

}
