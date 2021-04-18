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

public class TurnStile extends AbstractTurnStile {

    public TurnStile() {
        super();
        this.setRegistryName("turn_stile");
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
            super.onBlockPlacedBy(world, pos, state, player, stack);
        }
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
        if (!world.isRemote){
            removeUselessKey(world,pos,state);
        }
        super.onBlockHarvested(world, pos, state, entity);
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
}
