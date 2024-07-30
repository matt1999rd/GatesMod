package fr.mattmouss.gates.tileentity;


import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.TurnStile;

import fr.mattmouss.gates.gui.TSContainer;
import fr.mattmouss.gates.items.CardKeyItem;
import fr.mattmouss.gates.items.ModItem;

import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.blockTSPacket;
import fr.mattmouss.gates.tscapability.IdTSStorage;
import fr.mattmouss.gates.tscapability.TurnStileIdCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;

import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.world.level.block.state.BlockState;

public class TurnStileTileEntity extends AbstractTurnStileTileEntity implements IControlIdTE, MenuProvider {

    private final LazyOptional<IdTSStorage> idStorage = LazyOptional.of(this::getIdStorage).cast();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler).cast();

    public TurnStileTileEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlock.TURNSTILE_TILE_TYPE,blockPos,blockState);
    }

    @Nonnull
    public ItemStackHandler createHandler() {
        return new ItemStackHandler(1){

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    return (stack.getItem() == ModItem.CARD_KEY.asItem());
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if ( stack.getItem() != ModItem.CARD_KEY.asItem() && slot == 0) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Nonnull
    public IdTSStorage getIdStorage() {
        return new IdTSStorage();
    }


    public void tick(Level level,BlockState state) {
        if (this.isControlUnit()) {
            if (!level.isClientSide && !state.getValue(TurnStile.WAY_IS_ON)) {
                findPlayerGoingThrough();
            }
            managePlayerMovement();
        }
    }

    //this function is opening the door using function notifyTileEntityOfCardId when the player is well-placed
    private void findPlayerGoingThrough() {
        //to get the player in a 2 block distance from the following middle pos of the block
        double x = worldPosition.getX()+0.5D;
        double y = worldPosition.getY()+0.5D;
        double z = worldPosition.getZ()+0.5D;
        assert level != null;
        Player entity = level.getNearestPlayer(x,y,z,2,false);
        if (entity != null){
            //System.out.println("player find : "+entity);
            if (playerIsAtRightPos(entity)){
                DoorHingeSide dhs = this.getBlockState().getValue(BlockStateProperties.DOOR_HINGE);
                //from which side is the hinge we check the other hand

                notifyTileEntityOfCardId(entity, dhs == DoorHingeSide.RIGHT);
                // if return true : System.out.println("opening turn stile door and waiting for player");

            }
        }
        //System.out.println("no player nearby");
    }



    //this return true if ids match between id of card in player hand and id of the tile entity
    //it is also opening the door when it works
    private void notifyTileEntityOfCardId(Player player, boolean checkMainHand){
        ItemStack stack =(checkMainHand) ? player.getMainHandItem() : player.getItemInHand(InteractionHand.OFF_HAND);
        if (stack.getItem() == ModItem.CARD_KEY.asItem()){
            CardKeyItem key = (CardKeyItem)stack.getItem();
            int key_id = key.getId(stack);
            int te_id = getId();
            if (te_id == key_id){
                System.out.println("The way is open");
                //TODO : add here the sound ok
                for (BlockPos pos1 : this.getPositionOfBlockConnected()){
                    assert level != null;
                    TurnStileTileEntity tste = (TurnStileTileEntity) level.getBlockEntity(pos1);
                    assert tste != null;
                    tste.openTS();
                }
                //return true;
            }else {
                //TODO : add here the sound not ok
                System.out.println("the way is not allowed");
            }
        }
        //return false;
    }

    //return true when the player is in a box of 1 block with the center of the block
    // placed at the corner where the player is going to put its card
    private boolean playerIsAtRightPos(Player entity) {
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = this.getBlockState().getValue(BlockStateProperties.DOOR_HINGE);
        double x_detect = (increaseXCube(facing,dhs))? worldPosition.getX()+0.5:worldPosition.getX()-0.5;
        double z_detect = (increaseZCube(facing,dhs))? worldPosition.getZ()+0.5:worldPosition.getZ()-0.5;
        Vec3 pos = entity.position();
        double x_player = pos.x;
        double y_player = pos.y;
        double z_player = pos.z;
        if (x_detect<x_player && x_player<x_detect+1){
            if (y_player<pos.y()+1){
                // if false : System.out.println("player z position is not matching ! detect :"+z_detect+". posZ du player : "+z_player);
                return z_detect < z_player && z_player < z_detect + 1;
            } //else : System.out.println("player y position is not matching ! posY du player : "+y_player);

        } //else : System.out.println("player x position is not matching ! detect :"+x_detect+". posX du player : "+x_player);

        return false;
    }



    //return true when we need to increment the value of posX of 0.5
    private boolean increaseXCube(Direction facing, DoorHingeSide dhs) {
        return (facing == Direction.EAST) ||
                (facing == Direction.SOUTH && dhs == DoorHingeSide.LEFT) ||
                (facing == Direction.NORTH && dhs == DoorHingeSide.RIGHT);
    }

    //return true when we need to increment the value of posZ of 0.5
    private boolean increaseZCube(Direction facing, DoorHingeSide dhs) {
        return (facing == Direction.SOUTH) ||
                (facing == Direction.EAST && dhs == DoorHingeSide.RIGHT) ||
                (facing == Direction.WEST && dhs == DoorHingeSide.LEFT);
    }

    //other function for functionality in the te

    @Override
    public int getId() {
        AtomicInteger id_in = new AtomicInteger(-1);
        idStorage.ifPresent(s-> id_in.set(s.getId()));
        return id_in.get();
    }

    @Override
    public void changeId() {
        idStorage.ifPresent(ts->ts.changeId(level));
    }

    @Override
    public void setId(int id_in) {
        idStorage.ifPresent(s-> {
            assert level != null;
            if (level.isClientSide)s.setId(id_in);
            else s.setId(id_in,level);
        });

    }

    @Override
    public int getKeyId() {
        AtomicInteger id = new AtomicInteger(-1);
        handler.ifPresent(h -> {
            ItemStack stack = h.getStackInSlot(0);
            if (!stack.isEmpty()) {
                CardKeyItem card = (CardKeyItem)(stack.getItem().asItem());
                id.set(card.getId(stack));
            }
        });
        return id.get();
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        if (canWrite()){
            getCapability(TurnStileIdCapability.TURN_STILE_ID_STORAGE).ifPresent(e->{
                CompoundTag nbt =e.serializeNBT();
                tag.put("id_storage",nbt);
            });
            getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h->{
                CompoundTag nbt = ((INBTSerializable<CompoundTag>)h).serializeNBT();
                tag.put("inv",nbt);
            });
        }
        if (level != null){
            tag.putBoolean("isCU", isControlUnit());
        }else{
            tag.putBoolean("isCU",false);
        }
        return super.save(tag);
    }




    @Override
    public void load(CompoundTag tag) {
        boolean isRightTSB = tag.getBoolean("isCU");
        if (isRightTSB) {
            CompoundTag storage_tag;
            if (tag.contains("storage")){
                storage_tag = tag.getCompound("storage");
            }else {
                storage_tag = tag.getCompound("id_storage");
            }
            CompoundTag inv_tag = tag.getCompound("inv");
            getCapability(TurnStileIdCapability.TURN_STILE_ID_STORAGE).ifPresent(s -> s.deserializeNBT(storage_tag));
            getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> ((INBTSerializable<CompoundTag>) h).deserializeNBT(inv_tag));
        }
        super.load(tag);
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return handler.cast();
        }
        if (cap == TurnStileIdCapability.TURN_STILE_ID_STORAGE){
            return idStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return new TextComponent(Objects.requireNonNull(getType().getRegistryName()).getPath());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity) {
        assert level != null;
        return new TSContainer(i,level,worldPosition,playerInventory,playerEntity);
    }

    @Override
    protected void movePlayerGoingThrough(Player player, boolean fromExit) {
        BlockPos pos = getMainPos();
        if (this.isAnimationInWork() && !fromExit) {
            Networking.INSTANCE.sendToServer(new blockTSPacket(pos));
            for (BlockPos pos1 : this.getPositionOfBlockConnected()) {
                assert level != null;
                TurnStileTileEntity tste = (TurnStileTileEntity) level.getBlockEntity(pos1);
                assert tste != null;
                tste.blockTS();
            }
        }
        super.movePlayerGoingThrough(player, fromExit);
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }
}
