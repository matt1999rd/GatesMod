package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.items.TurnStileKeyItem;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketRemoveId;
import fr.mattmouss.gates.network.SetIdPacket;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.mattmouss.gates.enum_door.TurnSPosition.*;

public class TurnStile extends AbstractTurnStile {

    //control unit part (which is left if left hinge and right if right hinge) is the part that contains the id and the tech key position
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
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        if (player != null){
            if (!world.isClientSide) {
                //we change the id for the block Control Unit where the tech gui will open
                TurnStileTileEntity tste = (TurnStileTileEntity) world.getBlockEntity(pos);
                assert tste != null;
                tste.changeId();
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayerEntity)player),new SetIdPacket(pos,tste.getId()));
            }
            super.setPlacedBy(world, pos, state, player, stack);
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TurnSPosition position = stateIn.getValue(TS_POSITION);
        Direction blockFacing = stateIn.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (isInnerUpdate(position,facing,blockFacing) &&  !(facingState.getBlock() instanceof TurnStile)){
            onTurnStileRemoved(worldIn,currentPos);
            return Blocks.AIR.defaultBlockState();
        }
        if (position.isSolid() && facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            onTurnStileRemoved(worldIn,currentPos);
            return Blocks.AIR.defaultBlockState();
        }
        return stateIn;
    }

    //block facing is the direction of forth block
    private boolean isInnerUpdate(TurnSPosition position, Direction facingUpdate, Direction blockFacing){
        return ( position == RIGHT_BLOCK && facingUpdate == blockFacing.getClockWise()) ||
                (position == LEFT_BLOCK  && facingUpdate == blockFacing.getCounterClockWise()) ||
                (position == MAIN && (facingUpdate.getAxis() == blockFacing.getClockWise().getAxis() || facingUpdate == Direction.UP ) ) ||
                (position == UP_BLOCK && facingUpdate == Direction.DOWN);
    }

    private void removeUselessKey(IWorld world,BlockPos keyPos){
        TurnStileKeyItem key = (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem();
        ItemStack oldStack = new ItemStack(key);
        key.setTSPosition(oldStack, world, keyPos);
        List<? extends PlayerEntity> players = world.players();
        AtomicBoolean foundKey = new AtomicBoolean(false);
        players.forEach(p -> {
            PlayerInventory inventory = p.inventory;
            if (!foundKey.get()) {
                if (inventory.contains(oldStack)) {
                    int slot = inventory.findSlotMatchingItem(oldStack);
                    if (slot != -1) {
                        inventory.items.set(slot, ItemStack.EMPTY);
                        foundKey.set(true);
                    }
                }
            }
        });
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        onTurnStileRemoved(world,pos);
        super.playerWillDestroy(world, pos, state, entity);
    }

    private void onTurnStileRemoved(IWorld world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        TurnStileTileEntity tste = (TurnStileTileEntity) world.getBlockEntity(pos);
        assert tste != null;
        int id = tste.getId();
        if (isControlUnit(state) && !world.isClientSide()) {
            Networking.INSTANCE.sendToServer(new PacketRemoveId(pos,id));
            removeUselessKey(world,pos);
        }
    }
}
