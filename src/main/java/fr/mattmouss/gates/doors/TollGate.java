package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.items.TollKeyItem;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketRemoveId;
import fr.mattmouss.gates.network.SetIdPacket;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.mattmouss.gates.enum_door.TollGPosition.*;


public class TollGate extends AbstractTollGate {

    public TollGate() {
        super();
        this.setRegistryName("toll_gate");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TollGateTileEntity(blockPos,blockState);
    }

    @Override
    public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity entity, @Nonnull ItemStack stack) {
        if (entity != null){
            //we initialise the id
            if (!world.isClientSide ) {
                TollGateTileEntity tgte = (TollGateTileEntity) world.getBlockEntity(pos);
                assert tgte != null;
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer)entity),new SetIdPacket(pos,tgte.getId()));
            }
            super.setPlacedBy(world, pos, state, entity, stack);
        }
    }

    @Nonnull
    @Override
    public BlockState updateShape(BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        BlockState futureState = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (futureState.getBlock() == Blocks.AIR){
            onTollGateRemoved(worldIn,currentPos);
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }



    @Override
    public void playerWillDestroy(Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, Player entity) {
        System.out.println("destroying all block of toll gate");
        onTollGateRemoved(world,pos);
        super.playerWillDestroy(world, pos, state, entity);
    }

    private void removeUselessKey(LevelAccessor world,BlockPos pos){
        TollKeyItem key = (TollKeyItem) ModItem.TOLL_GATE_KEY.asItem();
        BlockState state = world.getBlockState(pos);
        ItemStack oldStack = new ItemStack(key);
        BlockPos keyPos = getKeyPos(pos,state);
        key.setTGPosition(oldStack, world, keyPos);
        List<? extends Player> players = world.players();
        AtomicBoolean foundKey = new AtomicBoolean(false);
        players.forEach(p -> {
            Inventory inventory = p.getInventory();
            if (!foundKey.get()) {
                if (inventory.contains(oldStack)) {
                    int slot = inventory.findSlotMatchingItem(oldStack);
                    if (slot != -1){
                        inventory.items.set(slot, ItemStack.EMPTY);
                        foundKey.set(true);
                    }
                }
            }
        });
    }

    private BlockPos getKeyPos(BlockPos pos, BlockState state) {
        TollGPosition tollGPosition = state.getValue(TG_POSITION);
        DoorHingeSide side = state.getValue(BlockStateProperties.DOOR_HINGE);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction extDirection = Functions.getDirectionOfExtBlock(facing,side);
        switch (tollGPosition){
            case EMPTY_EXT:
                return pos.relative(facing).relative(extDirection.getOpposite(),2);
            case UP_BLOCK:
                return pos.relative(facing).below();
            case MAIN:
                return pos.relative(facing).relative(extDirection.getOpposite());
            case EMPTY_BASE:
                return pos.relative(facing);
            case CONTROL_UNIT:
            default:
                return pos;
        }
    }

    private void onTollGateRemoved(LevelAccessor world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        TollGateTileEntity tgte = (TollGateTileEntity) world.getBlockEntity(pos);
        assert tgte != null;
        int id = tgte.getId();
        if (isControlUnit(state) && !world.isClientSide()) {
            Networking.INSTANCE.sendToServer(new PacketRemoveId(pos,id));
            removeUselessKey(world,pos);
        }
    }

    private boolean isControlUnit(BlockState state){
        return state.getValue(TG_POSITION) == CONTROL_UNIT;
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult blockRayTraceResult) {
        //old functionality of block

        TollGateTileEntity tgte = (TollGateTileEntity) world.getBlockEntity(pos);
        assert tgte != null;
        //we reload the player using the gui
        TollGateTileEntity.changePlayerId(entity);

        if (state.getValue(TG_POSITION) != TollGPosition.CONTROL_UNIT){
            return InteractionResult.FAIL;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = state.getValue(BlockStateProperties.DOOR_HINGE);

        //the player is a user
        if ((entity_looking_direction==facing.getCounterClockWise() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.getClockWise() && (dhs == DoorHingeSide.LEFT))){
            System.out.println("the player is a user ");
            System.out.println("opening user gui !!");
            ((TollGateTileEntity) Objects.requireNonNull(world.getBlockEntity(pos))).setSide(true);
            if (!world.isClientSide) {
                NetworkHooks.openGui((ServerPlayer) entity, tgte, tgte.getBlockPos());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> type) {
        return (type == ModBlock.TOLL_GATE_ENTITY_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof TollGateTileEntity) {
                ((TollGateTileEntity) t).tick(level1);
            }
        })) : null;
    }
}
