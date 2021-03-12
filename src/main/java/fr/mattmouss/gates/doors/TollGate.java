package fr.mattmouss.gates.doors;

import fr.mattmouss.gates.energystorage.IdTracker;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.items.TollKeyItem;
import fr.mattmouss.gates.items.TurnStileKeyItem;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.SetIdPacket;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
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
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;

import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
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
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        //on pose le block central au niveau du block selectionné
        //le coté de la barrière sera défini selon la position du joueur vis à vis du block
        if (entity != null){
            //on initialise l'id
            if (!world.isRemote ) {
                TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
                tgte.changeId();
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayerEntity)entity),new SetIdPacket(pos,tgte.getId()));
            }
            super.onBlockPlacedBy(world, pos, state, entity, stack);
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        TollGPosition position = stateIn.get(TG_POSITION);
        if (position == CONTROL_UNIT && facing == Direction.DOWN && !facingState.getMaterial().blocksMovement()) {
            if (!worldIn.isRemote()) removeUselessKey(worldIn.getWorld(), currentPos, stateIn);
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
        System.out.println("destroying all block of toll gate");
        TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
        if (!world.isRemote){
            IdTracker idTracker = world.getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(IdTracker::new,"idgates");
            idTracker.removeId(tgte.getId());
            removeUselessKey(world,pos,state);
        }
        super.onBlockHarvested(world, pos, state, entity);
    }

    private void removeUselessKey(World world,BlockPos pos,BlockState state){
        TollKeyItem key = (TollKeyItem) ModItem.TOLL_GATE_KEY.asItem();
        ItemStack oldStack = new ItemStack(key);
        BlockPos keyPos = getKeyPos(pos,state);
        key.setTGPosition(oldStack, world, keyPos);
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

    private BlockPos getKeyPos(BlockPos pos, BlockState state) {
        TollGPosition tollGPosition = state.get(TG_POSITION);
        DoorHingeSide side = state.get(BlockStateProperties.DOOR_HINGE);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Direction extDirection = Functions.getDirectionOfExtBlock(facing,side);
        switch (tollGPosition){
            case EMPTY_EXT:
                return pos.offset(facing).offset(extDirection.getOpposite(),2);
            case UP_BLOCK:
                return pos.offset(facing).down();
            case MAIN:
                return pos.offset(facing).offset(extDirection.getOpposite());
            case EMPTY_BASE:
                return pos.offset(facing);
            case CONTROL_UNIT:
            default:
                return pos;
        }
    }

    /*
    //1.15 function
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        //old functionnality of block

        TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
        assert tgte != null;
        //we reupload the player using the gui
        tgte.changePlayerId(entity);

        if (state.get(TG_POSITION) != TollGPosition.CONTROL_UNIT){
            return ActionResultType.FAIL;
        }
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = state.get(BlockStateProperties.DOOR_HINGE);

        //the player is a user
        if ((entity_looking_direction==facing.rotateYCCW() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.rotateY() && (dhs == DoorHingeSide.LEFT))){
            System.out.println("the player is a user ");
            System.out.println("openning user gui !!");
            ((TollGateTileEntity) world.getTileEntity(pos)).setSide(true);
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) entity, tgte, tgte.getPos());
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

     */


    //1.14.4 function onBlockActivated

    @Override
    public boolean func_220051_a(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand p_220051_5_, BlockRayTraceResult p_220051_6_) {
        //old functionnality of block

        TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
        assert tgte != null;
        //we reupload the player using the gui
        tgte.changePlayerId(player);

        if (state.get(TG_POSITION) != TollGPosition.CONTROL_UNIT){
            return false;
        }
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = Functions.getDirectionFromEntity(player,pos);
        DoorHingeSide dhs = state.get(BlockStateProperties.DOOR_HINGE);

        //the player is a user
        if ((entity_looking_direction==facing.rotateYCCW() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.rotateY() && (dhs == DoorHingeSide.LEFT))){
            System.out.println("the player is a user ");
            System.out.println("openning user gui !!");
            ((TollGateTileEntity) world.getTileEntity(pos)).setSide(true);
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) player, tgte, tgte.getPos());
            }
            return true;
        }
        return false;
    }

}
