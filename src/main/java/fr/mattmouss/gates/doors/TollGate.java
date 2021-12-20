package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.items.TollKeyItem;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketRemoveId;
import fr.mattmouss.gates.network.SetIdPacket;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

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
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TollGateTileEntity();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null){
            //we initialise the id
            if (!world.isClientSide ) {
                TollGateTileEntity tgte = (TollGateTileEntity) world.getBlockEntity(pos);
                assert tgte != null;
                tgte.changeId();
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayerEntity)entity),new SetIdPacket(pos,tgte.getId()));
            }
            super.setPlacedBy(world, pos, state, entity, stack);
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        BlockState futureState = super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (futureState.getBlock() == Blocks.AIR){
            onTollGateRemoved(worldIn,currentPos);
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }



    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of toll gate");
        onTollGateRemoved(world,pos);
        super.playerWillDestroy(world, pos, state, entity);
    }

    private void removeUselessKey(IWorld world,BlockPos pos){
        TollKeyItem key = (TollKeyItem) ModItem.TOLL_GATE_KEY.asItem();
        BlockState state = world.getBlockState(pos);
        ItemStack oldStack = new ItemStack(key);
        BlockPos keyPos = getKeyPos(pos,state);
        key.setTGPosition(oldStack, world, keyPos);
        List<? extends PlayerEntity> players = world.players();
        AtomicBoolean foundKey = new AtomicBoolean(false);
        players.forEach(p -> {
            PlayerInventory inventory = p.inventory;
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

    private void onTollGateRemoved(IWorld world, BlockPos pos){
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


    //1.15-1.16 function

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        //old functionality of block

        TollGateTileEntity tgte = (TollGateTileEntity) world.getBlockEntity(pos);
        assert tgte != null;
        //we reload the player using the gui
        TollGateTileEntity.changePlayerId(entity);

        if (state.getValue(TG_POSITION) != TollGPosition.CONTROL_UNIT){
            return ActionResultType.FAIL;
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
                NetworkHooks.openGui((ServerPlayerEntity) entity, tgte, tgte.getBlockPos());
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }



/*
    //1.14.4 function onBlockActivated

    @Override
    public boolean func_220051_a(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand p_220051_5_, BlockRayTraceResult p_220051_6_) {
        //old functionality of block

        TollGateTileEntity tgte = (TollGateTileEntity) world.getBlockEntity(pos);
        assert tgte != null;
        //we reload the player using the gui
        tgte.changePlayerId(player);

        if (state.getValue(TG_POSITION) != TollGPosition.CONTROL_UNIT){
            return false;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = Functions.getDirectionFromEntity(player,pos);
        DoorHingeSide dhs = state.getValue(BlockStateProperties.DOOR_HINGE);

        //the player is a user
        if ((entity_looking_direction==facing.getCounterClockWise() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.getClockWise() && (dhs == DoorHingeSide.LEFT))){
            System.out.println("the player is a user ");
            System.out.println("opening user gui !!");
            ((TollGateTileEntity) world.getBlockEntity(pos)).setSide(true);
            if (!world.isClientSide) {
                NetworkHooks.openGui((ServerPlayerEntity) player, tgte, tgte.getBlockPos());
            }
            return true;
        }
        return false;
    }

}
 */


}
