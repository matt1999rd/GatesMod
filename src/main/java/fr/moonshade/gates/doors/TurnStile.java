package fr.moonshade.gates.doors;


import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.enum_door.TurnSPosition;
import fr.moonshade.gates.items.ModItem;
import fr.moonshade.gates.items.TurnStileKeyItem;
import fr.moonshade.gates.network.Networking;
import fr.moonshade.gates.network.PacketRemoveId;
import fr.moonshade.gates.network.SetIdPacket;
import fr.moonshade.gates.tileentity.TurnStileTileEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class TurnStile extends AbstractTurnStile {

    //control unit part (which is left if left hinge and right if right hinge) is the part that contains the id and the tech key position
    public TurnStile() {
        super();
        this.setRegistryName("turn_stile");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TurnStileTileEntity(blockPos,blockState);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        if (player != null){
            if (!world.isClientSide) {
                //we change the id for the block Control Unit where the tech gui will open
                TurnStileTileEntity tste = (TurnStileTileEntity) world.getBlockEntity(pos);
                assert tste != null;
                tste.changeId();
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer)player),new SetIdPacket(pos,tste.getId()));
            }
            super.setPlacedBy(world, pos, state, player, stack);
        }
    }

    @Override
    public BlockState updateShape(@Nonnull BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        TurnSPosition position = stateIn.getValue(TS_POSITION);
        if (position.isSolid() && facing == Direction.DOWN && !facingState.getMaterial().blocksMotion()){
            onTurnStileRemoved(worldIn,currentPos);
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected BlockState getUpdateState(BlockState state, BlockState facingState) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING,facingState.getValue(BlockStateProperties.HORIZONTAL_FACING))
                .setValue(WAY_IS_ON,facingState.getValue(WAY_IS_ON));
    }

    private void removeUselessKey(LevelAccessor world,BlockPos keyPos){
        TurnStileKeyItem key = (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem();
        ItemStack oldStack = new ItemStack(key);
        key.setTSPosition(oldStack, world, keyPos);
        List<? extends Player> players = world.players();
        AtomicBoolean foundKey = new AtomicBoolean(false);
        players.forEach(p -> {
            Inventory inventory = p.getInventory();
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
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player entity) {
        onTurnStileRemoved(world,pos);
        super.playerWillDestroy(world, pos, state, entity);
    }

    private void onTurnStileRemoved(LevelAccessor world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        TurnStileTileEntity tste = (TurnStileTileEntity) world.getBlockEntity(pos);
        assert tste != null;
        int id = tste.getId();
        if (isControlUnit(state) && !world.isClientSide()) {
            Networking.INSTANCE.sendToServer(new PacketRemoveId(pos,id));
            removeUselessKey(world,pos);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> type) {
        return (type == ModBlock.TURNSTILE_TILE_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof TurnStileTileEntity) {
                ((TurnStileTileEntity) t).tick(level1,blockState);
            }
        })) : null;
    }
}
