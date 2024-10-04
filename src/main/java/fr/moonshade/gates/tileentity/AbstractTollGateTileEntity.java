package fr.moonshade.gates.tileentity;

import fr.moonshade.gates.doors.AbstractTollGate;
import fr.moonshade.gates.doors.TollGate;
import fr.moonshade.gates.enum_door.TollGPosition;
import fr.moonshade.gates.setup.ModSound;
import fr.moonshade.gates.tollcapability.ITollStorage;
import fr.moonshade.gates.tollcapability.TollStorage;
import fr.moonshade.gates.tollcapability.TollStorageCapability;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class AbstractTollGateTileEntity extends BlockEntity {
    public AbstractTollGateTileEntity(BlockEntityType<?> type,BlockPos pos,BlockState state) {
        super(type,pos,state);
    }

    public boolean manageAnimation() {
        //block for management of animation
        BlockState state = this.getBlockState();
        assert this.level != null;
        if (animationOpeningInProcess()) {
            int animationStep = state.getValue(TollGate.ANIMATION);
            if (animationStep == 0) {
                //add the sound of tollgate
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSound.TOLL_GATE_OPENING, 6.0F));
            }

            if (animationStep == 4) {
                setBoolOpen(false);
            } else {
                //add this condition to see the tollgate in opening process
                //if (animationStep !=3) {
                this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TollGate.ANIMATION, animationStep + 1));
                //}
            }
            return true;
        } else if (animationClosingInProcess()) {
            int animationStep = state.getValue(TollGate.ANIMATION);
            if (animationStep == 4) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSound.TOLL_GATE_CLOSING, 6.0F));
            }
            if (animationStep == 0) {
                setBoolClose(false);
            } else {
                this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TollGate.ANIMATION, animationStep - 1));
            }
            return true;
        }
        return false;
    }

    protected final LazyOptional<TollStorage> storage = LazyOptional.of(this::getStorage).cast();

    private TollStorage getStorage() {
        return new TollStorage();
    }

    private boolean animationOpeningInProcess(){
        return storage.map(ITollStorage::isOpening).orElse(false);
    }

    private boolean animationClosingInProcess(){
        return storage.map(ITollStorage::isClosing).orElse(false);
    }

    private void setBoolOpen(Boolean bool){
        storage.ifPresent(s -> s.setBoolOpen(bool));
    }

    private void setBoolClose(Boolean bool){
        storage.ifPresent(s -> s.setBoolClose(bool));
    }

    public void startAnimation(){
        BlockState state = this.getBlockState();
        int animationStep = state.getValue(TollGate.ANIMATION);
        if (animationStep == 0) {
            setBoolOpen(true); //starting opening animation
            System.out.println("starting animation open");
        }else if (animationStep == 4){
            setBoolClose(true); //starting closing animation
            System.out.println("starting animation close");
        }
    }

    //start all animation of all the block that make the tollgate

    protected void startAllAnimation(){
        startAnimation();
        if (!(this.getBlockState().getBlock() instanceof AbstractTollGate))return;
        AbstractTollGate tollGate = (AbstractTollGate) this.getBlockState().getBlock();
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        TollGPosition tgp = this.getBlockState().getValue(TollGate.TG_POSITION);
        DoorHingeSide dhs = this.getBlockState().getValue(BlockStateProperties.DOOR_HINGE);
        List<BlockPos> posList = tollGate.getPositionOfBlockConnected(direction,tgp,dhs,this.worldPosition);
        for (BlockPos pos1 : posList){
            assert level != null;
            if (!(Objects.requireNonNull(level.getBlockEntity(pos1)).getType().equals(this.getType()))) {
                throw new IllegalArgumentException("No tile entity on this blockPos :"+pos1);
            }
            //System.out.println("position of animated block :"+pos1);
            AbstractTollGateTileEntity atgte2 = (AbstractTollGateTileEntity) level.getBlockEntity(pos1);
            assert atgte2 != null;
            atgte2.startAnimation();
        }
    }

    //check if this TE is the control unit tile entity to avoid multiple definition of toll-storage that will be of no use
    public boolean isRightTE() {
        BlockState state = getBlockState();
        return state.getValue(TollGate.TG_POSITION) == TollGPosition.CONTROL_UNIT;
    }

    @Override
    public void load( CompoundTag compound) {
        boolean isRightTE = compound.getBoolean("isCU");
        if (isRightTE) {
            CompoundTag storage_tag = compound.getCompound("storage");
            getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(s -> ((INBTSerializable<CompoundTag>) s).deserializeNBT(storage_tag)); }
        super.load(compound);
    }

    protected boolean canWrite(){
        if (level == null){
            return true;
        }else {
            return isRightTE();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (canWrite()) {
            getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(storage -> {
                CompoundTag compoundNBT = ((INBTSerializable<CompoundTag>) storage).serializeNBT();
                tag.put("storage", compoundNBT);
            });
        }
        if (level != null){
            tag.putBoolean("isCU",isRightTE());
        }else{
            tag.putBoolean("isCU",false);
        }
        super.saveAdditional(tag);
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
