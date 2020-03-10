package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.animationboolean.AnimationBoolean;
import fr.mattmouss.gates.animationboolean.AnimationBooleanCapability;
import fr.mattmouss.gates.animationboolean.IAnimationBoolean;
import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.gui.TGTechContainer;
import fr.mattmouss.gates.gui.TGUserContainer;
import fr.mattmouss.gates.gui.TGTechnicianScreen;

import fr.mattmouss.gates.pricecap.PriceStorage;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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

public class TollGateTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    public TollGateTileEntity() {
        super(ModBlock.TOLL_GATE_ENTITY_TYPE);
    }

    private LazyOptional<AnimationBoolean> startAnimation = LazyOptional.of(this::getAnimation).cast();

    private LazyOptional<IEnergyStorage> price = LazyOptional.of(this::getPriceValue).cast();

    private static int amount_paid = 0;

    private static int timer = 0;

    private static int common_price =1;

    private static boolean UserGuiOpen = true;

    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler).cast();

    private AnimationBoolean getAnimation(){
        return new AnimationBoolean();
    }

    private IEnergyStorage getPriceValue(){ return new PriceStorage(64,0);}

    //true for user gui
    //false for teechnician gui
    public void setSide(boolean newSide){
        UserGuiOpen = newSide;
    }

    public ItemStackHandler createHandler() {
        return new ItemStackHandler(1){

            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == Items.EMERALD;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (stack.getItem() != Items.EMERALD) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
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
        if (this.getBlockState().get(TollGate.ANIMATION)==4){
            if (timer>400){
                startAllAnimation();
                timer =0;
            }else{
                System.out.println("le timer d'ouverture : "+timer);
                timer++;
            }
        }
    }

    private void manageEmeraldConsumption() {
        int price_to_pay = price.map(iEnergyStorage -> {
            return iEnergyStorage.getEnergyStored();
        }).orElse(1);

        handler.ifPresent(h -> {
            ItemStack stack = h.getStackInSlot(0);
            int number_of_emerald = stack.getCount();
            System.out.println("remaining payment :"+this.getRemainingPayment());
            //when payment is not completely done
            if (stack.getItem()==Items.EMERALD ){
                if (number_of_emerald >= this.getRemainingPayment()){
                    System.out.println("payment done !");
                    //beginning of open animation
                    startAllAnimation();
                    //we extract the emerald in the slot
                    h.extractItem(0,price_to_pay,false);
                    //we reset amound paid for next client
                    amount_paid = 0;
                    markDirty();
                }else{
                    System.out.println("payment not done !");
                    //when the amount paid is not enough we just extract the amount given and we register it
                    h.extractItem(0,number_of_emerald,false);
                    this.raiseAmountPaid(number_of_emerald);
                }
            }

        });

    }

    private void openGate(){

    }

    private void checkStability() {
        TollGPosition tgp = getBlockState().get(TollGate.TG_POSITION);
        Block block = world.getBlockState(pos.down()).getBlock();
        if (block instanceof AirBlock || block instanceof BushBlock || block instanceof LeavesBlock){
            System.out.println("pas de block à "+pos.down()+" : destruction du block !!");
            destroyBlock();
            //return pour arrêter la fonction
            return;
        }
    }

    //démarre toute les animation pour tout les blocks

    private void startAllAnimation(){
        startAnimation();
        List<BlockPos> posList = getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            if (!(world.getTileEntity(pos1) instanceof TollGateTileEntity)) throw new IllegalArgumentException("No tile entity on this blockPos :"+pos1);
            System.out.println("position du block animé :"+pos1);
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
        //destruction de tout les blocks connectés et du block lui même
        for (BlockPos pos1 : getPositionOfBlockConnected()){
            TollGate selecGate = (TollGate) world.getBlockState(pos1).getBlock();
            selecGate.deleteBlock(pos1,world);
        }
    }

    public Direction getDirectionOfExtBlock(){
        Direction direction = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        DoorHingeSide dhs = this.getBlockState().get(BlockStateProperties.DOOR_HINGE);
        return  (dhs == DoorHingeSide.RIGHT)?direction.rotateYCCW():direction.rotateY();
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

    public int getPrice(){
        AtomicInteger price_in = new AtomicInteger(1);
        price.ifPresent(iEnergyStorage -> {
            if (!world.isRemote) {
                System.out.println("price obtenu :" + iEnergyStorage.getEnergyStored());
            }
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
        price.ifPresent(iPriceValue -> ((INBTSerializable<CompoundNBT>)iPriceValue).deserializeNBT(price_tag));
        price.ifPresent(iPriceValue -> {
            int price = price_tag.getInt("price");
            System.out.println("prix récupéré par la fonction :" + price);
        });
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
        Direction extDirection = getDirectionOfExtBlock();
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
