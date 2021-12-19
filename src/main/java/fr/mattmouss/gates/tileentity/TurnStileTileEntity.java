package fr.mattmouss.gates.tileentity;


import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.TurnStile;

import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.gui.TSContainer;
import fr.mattmouss.gates.items.CardKeyItem;
import fr.mattmouss.gates.items.ModItem;

import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.blockTSPacket;
import fr.mattmouss.gates.network.movePlayerPacket;
import fr.mattmouss.gates.setup.ModSound;
import fr.mattmouss.gates.tscapability.ITSStorage;
import fr.mattmouss.gates.tscapability.TSStorage;
import fr.mattmouss.gates.tscapability.TurnStileCapability;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TurnStileTileEntity extends TileEntity implements IControlIdTE,ITickableTileEntity, INamedContainerProvider {

    public TurnStileTileEntity() {
        super(ModBlock.TURNSTILE_TILE_TYPE);
    }

    private LazyOptional<ITSStorage> storage = LazyOptional.of(this::getStorage).cast();
    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler).cast();

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

    public ITSStorage getStorage() {
        return new TSStorage();
    }

    public List<BlockPos> getPositionOfBlockConnected() {
        List<BlockPos> posList = new ArrayList<>();
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos main_pos = getMainPos();
        posList.add(main_pos);
        posList.add(main_pos.relative(direction.getClockWise()));
        posList.add(main_pos.relative(direction.getCounterClockWise()));
        posList.add(main_pos.relative(Direction.UP));
        return posList;
    }

    public BlockPos getMainPos(){
        TurnSPosition tsp = this.getBlockState().getValue(TurnStile.TS_POSITION);
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch (tsp.getMeta()){
            case 0:
                return worldPosition;
            case 1:
                return worldPosition.relative(direction.getClockWise());
            case 2:
                return worldPosition.relative(direction.getCounterClockWise());
            case 3:
                return worldPosition.relative(Direction.DOWN);
            default:
                throw new IllegalArgumentException("unknown meta value for tsp :"+tsp.getMeta());
        }
    }

    @Override
    public void tick() {
        BlockState state = this.getBlockState();
        if (this.isControlUnit()) {
            if (!level.isClientSide && !state.getValue(TurnStile.WAY_IS_ON)) {
                findPlayerGoingThrough();
            }
            if (state.getValue(TurnStile.WAY_IS_ON)) {
                BlockPos mainPos = this.getMainPos();
                double x = mainPos.getX() + 0.5D;
                double y = mainPos.getY() + 0.5D;
                double z = mainPos.getZ() + 0.5D;
                PlayerEntity player = level.getNearestPlayer(x, y, z, 2, false);
                if (this.isAnimationInWork()){
                    movePlayerGoingThrough(player);
                    return;
                }
                if (playerIsGoingThrough(player)) {
                    movePlayerGoingThrough(player);
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.TURN_STILE_PASS,1.0F));
                }
            }
        }
    }

    //this function check if the player in argument is going into this turn stile with a certain speed
    private boolean playerIsGoingThrough(PlayerEntity entity) {
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos mainPos = this.getMainPos();
        double x = mainPos.getX();
        double y = mainPos.getY();
        double z = mainPos.getZ();
        if (entity != null){
            Vector3d vec3d = entity.getDeltaMovement();
            //the player is going in the direction of the turn stile
            boolean isRightMove = (Direction.getNearest(vec3d.x,vec3d.y,vec3d.z) == facing.getOpposite());
            Vector3d player_pos = entity.position();
            double x_player = player_pos.x;
            double y_player = player_pos.y;
            double z_player = player_pos.z;
            boolean isPlayerInFrontOfMainBlock;
            Direction.Axis axis = facing.getAxis();
            double coor_player =axis.choose(x_player,y_player,z_player);
            double coor_pos = axis.choose(x,y,z);
            int axisDirOffset = facing.getAxisDirection().getStep();
            //it is a very simplified expression which check in each direction for placement of player
            // if NORTH or SOUTH it will check the coordinate z and verify if
            // for NORTH posZ-0.5<z<posZ for SOUTH posZ+1<z<posZ+1.5
            // if EAST or WEST it will check th coordinate x and verify if
            // for WEST posX-0.5<x<posX for EAST posX+1<x<posX+1.5
            isPlayerInFrontOfMainBlock = (coor_player>coor_pos+0.25+axisDirOffset*0.75) && (coor_player<coor_pos+0.75+0.75*axisDirOffset);
            return isRightMove && isPlayerInFrontOfMainBlock;
        }
        return false;
    }

    //this function is aimed to move the player into the turn stile
    private void movePlayerGoingThrough(PlayerEntity player) {
        if (player == null){
            return;
        }
        this.changeAllAnim();

        BlockPos pos = getMainPos();
        System.out.println("moving player into turn stile !!");

        if (this.isAnimationInWork()){
            Networking.INSTANCE.sendToServer(new movePlayerPacket(pos, (ClientPlayerEntity) player,true,false));
            this.endAnimation();
            Networking.INSTANCE.sendToServer(new blockTSPacket(pos,false));
            for (BlockPos pos1 : this.getPositionOfBlockConnected()) {
                TurnStileTileEntity tste = (TurnStileTileEntity) level.getBlockEntity(pos1);
                tste.blockTS();
            }
        }else {
            Networking.INSTANCE.sendToServer(new movePlayerPacket(pos,(ClientPlayerEntity)player,false,false));
            //player.moveToBlockPosAndAngles(pos,rot.y,rot.x);
            this.startAnimation();
        }
    }

    //this function is opening the door using function notifyTileEntityOfCardId when the player is well placed
    private void findPlayerGoingThrough() {
        //to get the player in a 2 block distance from the following middle pos of the block
        double x = worldPosition.getX()+0.5D;
        double y = worldPosition.getY()+0.5D;
        double z = worldPosition.getZ()+0.5D;
        PlayerEntity entity = level.getNearestPlayer(x,y,z,2,false);
        if (entity != null){
            //System.out.println("player find : "+entity);
            if (playerIsAtRightPos(entity)){
                DoorHingeSide dhs = this.getBlockState().getValue(BlockStateProperties.DOOR_HINGE);
                //from which side is the hinge we check the other hand
                if (notifyTileEntityOfCardId(entity,dhs == DoorHingeSide.RIGHT)){
                    System.out.println("opening turn stile door and waiting for player");
                }
            }
        }
        //System.out.println("no player nearby");
    }



    //this return true if ids match between id of card in player hand and id of the tile entity
    //it is also opening the door when it works
    private boolean notifyTileEntityOfCardId(PlayerEntity player,boolean checkMainHand){
        ItemStack stack =(checkMainHand) ? player.getMainHandItem() : player.getItemInHand(Hand.OFF_HAND);
        if (stack.getItem() == ModItem.CARD_KEY.asItem()){
            CardKeyItem key = (CardKeyItem)stack.getItem();
            int key_id = key.getId(stack);
            int te_id = getId();
            if (te_id == key_id){
                System.out.println("The way is open");
                //TODO : add here the sound ok
                for (BlockPos pos1 : this.getPositionOfBlockConnected()){
                    TurnStileTileEntity tste = (TurnStileTileEntity) level.getBlockEntity(pos1);
                    tste.openTS();
                }
                return true;
            }else {
                //TODO : add here the sound not ok
                System.out.println("the way is not allowed");
            }
        }
        return false;
    }

    //return true when the player is in a box of 1 block with the center of the block
    // placed at the corner where the player is going to put its card
    private boolean playerIsAtRightPos(PlayerEntity entity) {
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = this.getBlockState().getValue(BlockStateProperties.DOOR_HINGE);
        double x_detect = (increaseXCube(facing,dhs))? worldPosition.getX()+0.5:worldPosition.getX()-0.5;
        double z_detect = (increaseZCube(facing,dhs))? worldPosition.getZ()+0.5:worldPosition.getZ()-0.5;
        Vector3d pos = entity.position();
        double x_player = pos.x;
        double y_player = pos.y;
        double z_player = pos.z;
        if (x_detect<x_player && x_player<x_detect+1){
            if (y_player<pos.y()+1){
                if (z_detect<z_player && z_player<z_detect+1){
                    return true;
                }else {
                    System.out.println("player z position is not matching ! detect :"+z_detect+". posZ du player : "+z_player);
                }
            }else {
                System.out.println("player y position is not matching ! posY du player : "+y_player);
            }
        }else {
            System.out.println("player x position is not matching ! detect :"+x_detect+". posX du player : "+x_player);
        }
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

    public void changeAllAnim() {
        List<BlockPos> posList = getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            TurnStileTileEntity tste = (TurnStileTileEntity) level.getBlockEntity(pos1);
            tste.changeAnim();
        }
    }

    public void changeAnim(){
        BlockState state = this.getBlockState();
        int i = state.getValue(TurnStile.ANIMATION);
        level.setBlockAndUpdate(worldPosition,state.setValue(TurnStile.ANIMATION,1-i));
    }

    @Override
    public int getId() {
        AtomicInteger id_in = new AtomicInteger(-1);
        storage.ifPresent(s->{
            id_in.set(s.getId());
        });
        return id_in.get();
    }

    @Override
    public void changeId() {
        storage.ifPresent(ts->ts.changeId(level));
    }

    @Override
    public void setId(int id_in) {
        storage.ifPresent(s-> {
            if (level.isClientSide)s.setId(id_in);
            else s.setId(id_in,level);
        });

    }


    public void openTS(){
        BlockState state =this.getBlockState();
        level.setBlockAndUpdate(worldPosition,state.setValue(TurnStile.WAY_IS_ON,true));
    }

    public void blockTS(){
        BlockState state = this.getBlockState();
        level.setBlockAndUpdate(worldPosition,state.setValue(TurnStile.WAY_IS_ON,false));
    }

    @Override
    public int getKeyId() {
        AtomicInteger id = new AtomicInteger(-1);
        handler.ifPresent(h -> {
            ItemStack stack = h.getStackInSlot(0);
            if (stack.isEmpty()){
                return;
            }else {
                CardKeyItem card = (CardKeyItem)(stack.getItem().asItem());
                id.set(card.getId(stack));
            }
        });
        return id.get();
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        if (canWrite()){
            getCapability(TurnStileCapability.TURN_STILE_STORAGE).ifPresent(e->{
                CompoundNBT nbt =((INBTSerializable<CompoundNBT>)e).serializeNBT();
                tag.put("storage",nbt);
            });
            getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h->{
                CompoundNBT nbt = ((INBTSerializable<CompoundNBT>)h).serializeNBT();
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

    private boolean canWrite() {
        if (level == null){
            return true;
        }else {
            return isControlUnit() ;
        }
    }

    private boolean isAnimationInWork(){
        return storage.map(ITSStorage::getAnimationInWork).orElse(false);
    }

    private void startAnimation(){
        storage.ifPresent(ITSStorage::startAnimation);
    }

    private void endAnimation(){
        storage.ifPresent(ITSStorage::endAnimation);
    }


    @Override
    public void load(BlockState state,CompoundNBT tag) {
        boolean isRightTSB = tag.getBoolean("isCU");
        if (isRightTSB) {
            CompoundNBT storage_tag = tag.getCompound("storage");
            CompoundNBT inv_tag = tag.getCompound("inv");
            getCapability(TurnStileCapability.TURN_STILE_STORAGE).ifPresent(s -> ((INBTSerializable<CompoundNBT>) s).deserializeNBT(storage_tag));
            getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(inv_tag));
        }
        super.load(state,tag);
    }

    //check if this TE is the control unit tile entity to avoid multiple definition of idstorage that will be of no use
    public boolean isControlUnit() {
        Block block=this.getBlockState().getBlock();
        if (!(block instanceof TurnStile)){
            return false;
        }
        TurnStile turnStile = (TurnStile)block;
        return turnStile.isControlUnit(this.getBlockState());
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return handler.cast();
        }
        if (cap == TurnStileCapability.TURN_STILE_STORAGE){
            return storage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new TSContainer(i,level,worldPosition,playerInventory,playerEntity);
    }
}
