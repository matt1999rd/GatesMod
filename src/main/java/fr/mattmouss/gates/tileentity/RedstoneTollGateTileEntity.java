package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.RedstoneTollGate;
import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.setup.ModSound;
import fr.mattmouss.gates.tollcapability.ITollStorage;
import fr.mattmouss.gates.tollcapability.TollStorage;
import fr.mattmouss.gates.tollcapability.TollStorageCapability;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RedstoneTollGateTileEntity extends TileEntity implements ITickableTileEntity {

    private LazyOptional<TollStorage> storage = LazyOptional.of(this::getStorage).cast();
    private boolean lastPowered = false;

    private TollStorage getStorage() {
        return new TollStorage();
    }

    public RedstoneTollGateTileEntity() {
        super(ModBlock.RTOLL_GATE_ENTITY_TYPE);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            BlockState state = this.getBlockState();
            if (lastPowered != state.get(BlockStateProperties.POWERED)){
                lastPowered = true;
                startAllAnimation();
            }
            //block for gestion of animation
            if (animationOpeningInProcess()) {
                int animationStep = state.get(TollGate.ANIMATION);
                if (animationStep == 0) {
                    //add the sound of toll gate
                    Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(ModSound.TOLL_GATE_OPENING, 6.0F));
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
                    Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(ModSound.TOLL_GATE_CLOSING, 6.0F));
                }
                if (animationStep == 0) {
                    setBoolClose(false);
                } else {
                    this.world.setBlockState(this.pos, state.with(TollGate.ANIMATION, animationStep - 1));
                }
            }
        }
    }

    //start all animation of all the block that make the toll gate

    private void startAllAnimation(){
        startAnimation();
        if (!(this.getBlockState().getBlock() instanceof RedstoneTollGate))return;
        RedstoneTollGate tollGate = (RedstoneTollGate) this.getBlockState().getBlock();
        Direction direction = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        TollGPosition tgp = this.getBlockState().get(TollGate.TG_POSITION);
        DoorHingeSide dhs = this.getBlockState().get(BlockStateProperties.DOOR_HINGE);
        List<BlockPos> posList = tollGate.getPositionOfBlockConnected(direction,tgp,dhs,this.pos);
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

    private boolean animationOpeningInProcess(){
        return storage.map(ITollStorage::isOpening).orElse(false);
    }

    private boolean animationClosingInProcess(){
        return storage.map(ITollStorage::isClosing).orElse(false);
    }

    private void setBoolOpen(Boolean bool){
        storage.ifPresent(s ->{
            s.setBoolOpen(bool);
        });
    }

    private void setBoolClose(Boolean bool){
        storage.ifPresent(s -> {
            s.setBoolClose(bool);
        });
    }

    //check if this TE is the control unit tile entity to avoid multiple definition of tollstorage that will be of no use
    public boolean isRightTE() {
        BlockState state = getBlockState();
        return state.get(TollGate.TG_POSITION) == TollGPosition.CONTROL_UNIT;
    }

    @Override
    public void read(CompoundNBT compound) {
        boolean isRightTE = compound.getBoolean("isCU");
        if (isRightTE) {
            CompoundNBT storage_tag = compound.getCompound("storage");
            getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(s -> ((INBTSerializable<CompoundNBT>) s).deserializeNBT(storage_tag)); }
        super.read(compound);
    }



    private boolean canWrite(){
        if (world == null){
            return true;
        }else {
            return isRightTE();
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        if (canWrite()) {
            getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(storage -> {
                CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>) storage).serializeNBT();
                tag.put("storage", compoundNBT);
            });
        }
        if (world != null){
            tag.putBoolean("isCU",isRightTE());
        }else{
            tag.putBoolean("isCU",false);
        }
        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == TollStorageCapability.TOLL_STORAGE){
            return storage.cast();
        }
        return super.getCapability(cap, side);
    }




}
