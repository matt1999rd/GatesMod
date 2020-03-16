package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.animationboolean.AnimationBoolean;
import fr.mattmouss.gates.animationboolean.AnimationBooleanCapability;
import fr.mattmouss.gates.animationboolean.IAnimationBoolean;
import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.energystorage.IdStorage;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.gui.TGTechContainer;
import fr.mattmouss.gates.gui.TGUserContainer;

import fr.mattmouss.gates.items.CardKeyItem;
import fr.mattmouss.gates.items.ModItem;
import fr.mattmouss.gates.energystorage.PriceStorage;

import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TollGateTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider,IControlIdTE {



    public TollGateTileEntity() {
        super(ModBlock.TOLL_GATE_ENTITY_TYPE);
    }

    private LazyOptional<AnimationBoolean> startAnimation = LazyOptional.of(this::getAnimation).cast();

    private LazyOptional<IEnergyStorage> price = LazyOptional.of(this::getPriceValue).cast();

    private LazyOptional<IEnergyStorage> id = LazyOptional.of(this::getIdValue).cast();

    private static int amount_paid = 0;

    private static boolean UserGuiOpen = true;

    private static int last_user_player_id = 0;

    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler).cast();

    private AnimationBoolean getAnimation(){
        return new AnimationBoolean();
    }

    private IEnergyStorage getPriceValue(){ return new PriceStorage(64,0);}

    public IEnergyStorage getIdValue(){
        return new IdStorage();
    }


    //true for user gui
    //false for technician gui
    public void setSide(boolean newSide){
        UserGuiOpen = newSide;
    }

    public ItemStackHandler createHandler() {
        return new ItemStackHandler(2){

            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0){
                    return (stack.getItem() == Items.EMERALD);
                }else {
                    return stack.getItem() == ModItem.CARD_KEY.asItem();
                }

            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if ((stack.getItem() != Items.EMERALD && slot ==0) || (stack.getItem() != ModItem.CARD_KEY.asItem() && slot == 1)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    public static void changePlayerId(PlayerEntity entity) {
        last_user_player_id = entity.getEntityId();
    }

    @Override
    public void tick() {
        //updatePrice();
        if (!world.isRemote) {
            BlockState state = this.getBlockState();
            //block for gestion of animation
            if (animationOpeningInProcess()) {
                int animationStep = state.get(TollGate.ANIMATION);
                if (animationStep == 0) {
                    //add the sound of toll gate
                    //Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(ModSound.ANIMATION_TOLL_GATE, 1.0F));
                }

                if (animationStep == 4) {
                    setBoolOpen(false);
                } else {
                    //add this condition to see the toll gate in opening process
                    //if (animationStep !=3) {
                    this.world.setBlockState(this.pos, state.with(TollGate.ANIMATION, animationStep + 1));
                    //}
                }
            } else if (animationClosingInProcess()) {
                int animationStep = state.get(TollGate.ANIMATION);
                if (animationStep == 4) {
                    //Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(ModSound.ANIMATION_TOLL_GATE, 1.0F));
                }
                if (animationStep == 0) {
                    setBoolClose(false);
                } else {
                    this.world.setBlockState(this.pos, state.with(TollGate.ANIMATION, animationStep - 1));
                }
            } else {
                checkStability();
                manageEmeraldConsumption();
                checkTimer();
            }
        }
    }

    private void checkTimer() {
        if (this.getBlockState().get(TollGate.ANIMATION)==4) {
            PlayerEntity entity = (PlayerEntity) world.getEntityByID(last_user_player_id);
            if (entity == null){
                return;
            }
            double[] entity_pos = new double[3];
            Vec3d vec3d = entity.getPositionVector();
            entity_pos[0] = vec3d.x;
            entity_pos[1] = vec3d.y;
            entity_pos[2] = vec3d.z;
            double[] block_pos = new double[3];
            block_pos[0] = pos.getX();
            block_pos[1] = pos.getY();
            block_pos[2] = pos.getZ();
            System.out.println(Functions.Distance3(block_pos,entity_pos));

            if (Functions.Distance3(block_pos, entity_pos) < 10) {
                return;
            }
            last_user_player_id =0;
            System.out.println("end of barrier open");
            startAllAnimation();
        }

        /*
        if (this.getBlockState().get(TollGate.ANIMATION)==4){
            if (timer>400){
                startAllAnimation();
                timer =0;
            }else{
                System.out.println("le timer d'ouverture : "+timer);
                timer++;
            }
        }
         */
    }

    private void manageEmeraldConsumption() {
        int price_to_pay = price.map(iEnergyStorage -> {
            return iEnergyStorage.getEnergyStored();
        }).orElse(1);

        handler.ifPresent(h -> {
            ItemStack stack0 = h.getStackInSlot(0);
            ItemStack stack1 = h.getStackInSlot(1);
            int number_of_emerald = stack0.getCount();
            //System.out.println("remaining payment :"+this.getRemainingPayment());
            //if the animation is in process or the toll gate is open it will stop the management of payment
            if (this.getBlockState().get(TollGate.ANIMATION)!=0){
                return;
            }
            if (last_user_player_id ==0){
                return;
            }
            PlayerEntity entity = (PlayerEntity) world.getEntityByID(last_user_player_id);
            //when payment is not completely done
            if (stack0.getItem()==Items.EMERALD ){
                if (number_of_emerald >= this.getRemainingPayment()){
                    //System.out.println("payment done !");
                    //beginning of open animation
                    startAllAnimation();
                    //we extract the emerald in the slot
                    h.extractItem(0,number_of_emerald,false);
                    ItemStack stack2 = new ItemStack(Items.EMERALD.asItem());
                    stack2.setCount(number_of_emerald-price_to_pay);

                    //drop extra amount of emerald
                    if (entity != null) {
                        entity.dropItem(stack2, false);
                    }
                    //we reset amound paid for next client
                    amount_paid = 0;
                    markDirty();
                }else{
                    //System.out.println("payment not done !");
                    //when the amount paid is not enough we just extract the amount given and we register it
                    h.extractItem(0,number_of_emerald,false);
                    this.raiseAmountPaid(number_of_emerald);
                }
            }else if (stack1.getItem() == ModItem.CARD_KEY.asItem()){
                CardKeyItem key = (CardKeyItem)stack1.getItem();
                BlockPos key_pos = key.getTGPosition(stack1,world);
                if (key_pos == null){
                    return;
                }
                if (pos.equals(key_pos)){
                    h.extractItem(1,1,false);
                    //drop the card because it will be used later on
                    entity.dropItem(stack1,false);
                    startAllAnimation();
                    markDirty();
                }
            }

        });

    }


    private void checkStability() {
        TollGPosition tgp = getBlockState().get(TollGate.TG_POSITION);
        Block block = world.getBlockState(pos.down()).getBlock();
        if (!tgp.isDownBlock()){
            //no stability to check
            return;
        }
        if (block instanceof AirBlock || block instanceof BushBlock || block instanceof LeavesBlock){
            //System.out.println("pas de block à "+pos.down()+" : destruction du block !!");
            destroyBlock();
            //return for stoping the function
            return;
        }
    }

    //start all animation of all the block that make the toll gate

    private void startAllAnimation(){
        startAnimation();
        List<BlockPos> posList = getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            if (!(world.getTileEntity(pos1) instanceof TollGateTileEntity)) throw new IllegalArgumentException("No tile entity on this blockPos :"+pos1);
            //System.out.println("position du block animé :"+pos1);
            TollGateTileEntity tgte2 = (TollGateTileEntity) world.getTileEntity(pos1);
            assert tgte2 != null;
            tgte2.startAnimation();
        }
    }

    public void startAnimation(){
        BlockState state = this.getBlockState();
        int animationStep = state.get(TollGate.ANIMATION);
        if (animationStep == 0) {
            setBoolOpen(true); //mettre en route l'animation d'ouverture
            System.out.println("starting animation open");
        }else if (animationStep == 4){
            setBoolClose(true); //mettre en route l'animation de fermeture
            System.out.println("starting animation close");
        }
    }

    public void startAnimation(boolean opening){
        BlockState state = this.getBlockState();
        int animationStep = state.get(TollGate.ANIMATION);
        if (animationStep == 0 && opening) {
            setBoolOpen(true); //mettre en route l'animation d'ouverture
            //System.out.println("starting animation open");
        }else if (animationStep == 4 && !opening){
            setBoolClose(true); //mettre en route l'animation de fermeture
            //System.out.println("starting animation close");
        }
    }

    private void destroyBlock() {
        //destruction of all blocks connected and of the block itself
        for (BlockPos pos1 : getPositionOfBlockConnected()){
            TollGate selecGate = (TollGate) world.getBlockState(pos1).getBlock();
            selecGate.deleteBlock(pos1,world);
        }
    }

    private boolean animationOpeningInProcess(){
        return startAnimation.map(IAnimationBoolean::isOpening).orElse(false);
    }

    private boolean animationClosingInProcess(){
        return startAnimation.map(IAnimationBoolean::isClosing).orElse(false);
    }

    private void setBoolOpen(Boolean bool){
        startAnimation.ifPresent(animationBoolean ->{
            animationBoolean.setBoolOpen(bool);
        });
    }

    private void setBoolClose(Boolean bool){
        startAnimation.ifPresent(animationBoolean -> {
            animationBoolean.setBoolClose(bool);
        });
    }
    /*
    public void updatePrice(){
        if (!world.isRemote){
            common_price =price.map(IEnergyStorage::getEnergyStored).orElse(1);
        }else {
            price.ifPresent(iEnergyStorage -> {
                ((PriceStorage)iEnergyStorage).setValue(common_price);
            });
        }

    }

     */


    @Override
    public void setId(int id_in) {
        id.ifPresent(e->{
            ((IdStorage)e).changeId(id_in);
        });
    }

    public void changeId(){
        if (!world.isRemote) {
            id.ifPresent(e -> {
                ((IdStorage) e).changeId(world.getServer().getWorld(DimensionType.OVERWORLD));
            });
        }
    }

    public int getId(){
        AtomicInteger id_in = new AtomicInteger(1);
        id.ifPresent(e->{
            id_in.set(e.getEnergyStored());
        });
        return id_in.get();
    }


    public int getPrice(){
        AtomicInteger price_in = new AtomicInteger(1);
        price.ifPresent(iEnergyStorage -> {
            price_in.set(iEnergyStorage.getEnergyStored());
        });
        return price_in.get();
    }

    public void lowerPrice(){
        price.ifPresent(e -> {
            ((PriceStorage) e).lowerPrice();
        });
        if (getBlockState().get(TollGate.TG_POSITION) == TollGPosition.CONTROL_UNIT) {
            List<BlockPos> posList = getPositionOfBlockConnected();
            for (BlockPos pos1 : posList) {
                if (pos1.getX() != pos.getX() || pos1.getY() != pos.getY() || pos1.getZ() != pos.getZ()) {
                    TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos1);
                    if (tgte == null){
                        System.out.println("la tile entity est null");
                        return;
                    }
                    tgte.lowerPrice();
                }
            }
        }
    }

    public void raisePrice(){
        System.out.println("raising price..");
        price.ifPresent(e -> {
            ((PriceStorage)e).raisePrice();
        });
        if (getBlockState().get(TollGate.TG_POSITION)==TollGPosition.CONTROL_UNIT){
            List<BlockPos> posList = getPositionOfBlockConnected();
            for (BlockPos pos1 : posList){
                if (pos1.getX()!=pos.getX() || pos1.getY()!=pos.getY() || pos1.getZ() != pos.getZ()){
                    TollGateTileEntity tgte = (TollGateTileEntity)world.getTileEntity(pos1);
                    if (tgte == null){
                        System.out.println("la tile entity est null");
                        return;
                    }
                    tgte.raisePrice();
                }
            }
        }
    }

    public void raiseAmountPaid(int newAmount){
        amount_paid += newAmount;
    }

    @Override
    public void read(CompoundNBT compound) {
        CompoundNBT invTag=compound.getCompound("inv");
        CompoundNBT price_tag = compound.getCompound("price");
        CompoundNBT anim_tag = compound.getCompound("anim");
        CompoundNBT id_tag = compound.getCompound("id");
        price.ifPresent(iPriceValue -> ((INBTSerializable<CompoundNBT>)iPriceValue).deserializeNBT(price_tag));
        id.ifPresent(e->((INBTSerializable<CompoundNBT>)e).deserializeNBT(id_tag));
        handler.ifPresent(h -> ((INBTSerializable<CompoundNBT>)h).deserializeNBT(invTag));
        startAnimation.ifPresent(animationBoolean -> ((INBTSerializable<CompoundNBT>)animationBoolean).deserializeNBT(anim_tag));
        super.read(compound);

    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        startAnimation.ifPresent(animationBoolean -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)animationBoolean).serializeNBT();
            tag.put("anim",compoundNBT);
        });
        handler.ifPresent(h -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)h).serializeNBT();
            tag.put("inv",compoundNBT);
        });
        price.ifPresent(iPriceValue -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>) iPriceValue).serializeNBT();
            tag.put("price", compoundNBT);
        });
        id.ifPresent(e->{
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)e).serializeNBT();
            tag.put("id",compoundNBT);
        });
        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY){
            return startAnimation.cast();
        }
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return handler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY){
            return price.cast();
        }
        return super.getCapability(cap, side);
    }

    //que les blocks connecté en prenant en compte le block lui même
    public List<BlockPos> getPositionOfBlockConnected() {
        //ajout de tout les blocks
        Direction direction = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        TollGPosition tgp = this.getBlockState().get(TollGate.TG_POSITION);
        List<BlockPos> posList = new ArrayList<>();
        DoorHingeSide dhs = this.getBlockState().get(BlockStateProperties.DOOR_HINGE);
        Direction extDirection = Functions.getDirectionOfExtBlock(direction,dhs);
        BlockPos emptyBasePos = getEmptyBasePos(tgp,extDirection,direction);
        //block emptybase
        posList.add(emptyBasePos);
        //block de control unit
        posList.add(emptyBasePos.offset(direction));
        //block main et emptyext
        posList.add(emptyBasePos.offset(extDirection));
        posList.add(emptyBasePos.offset(extDirection,2));
        //block up
        posList.add(emptyBasePos.up());
        return posList;
    }

    private BlockPos getEmptyBasePos(TollGPosition tgp, Direction extDirection, Direction facing) {
        switch (tgp){
            case EMPTY_BASE:
                return pos;
            case MAIN:
                return pos.offset(extDirection.getOpposite());
            case EMPTY_EXT:
                return pos.offset(extDirection.getOpposite(),2);
            case UP_BLOCK:
                return pos.down();
            case CONTROL_UNIT:
                return pos.offset(facing.getOpposite());
            default:
                throw new NullPointerException("TollGatePosition of block at position :"+this.pos+"has null attribut for tollgateposition");
        }

    }
    //this function as public give the position of toll gate registering
    public BlockPos getCentralPos(){
        return null;
    }

    public boolean isGateOpen(){
        int animation_step = this.getBlockState().get(TollGate.ANIMATION);
        return animation_step != 0;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (UserGuiOpen){
            return new TGUserContainer(i,world,pos,playerInventory,playerEntity);
        }else{
            return new TGTechContainer(i,world,pos,playerEntity);
        }
    }

    public int getRemainingPayment() {
        return getPrice()-amount_paid;
    }
}
