package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.RedstoneTollGate;
import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.setup.ModSound;
import fr.mattmouss.gates.tollcapability.ITollStorage;
import fr.mattmouss.gates.tollcapability.TollStorage;
import fr.mattmouss.gates.tollcapability.TollStorageCapability;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RedstoneTollGateTileEntity extends TileEntity implements ITickableTileEntity {

    private final LazyOptional<TollStorage> storage = LazyOptional.of(this::getStorage).cast();
    private boolean lastPowered = false;
    private boolean initialise = false;

    private TollStorage getStorage() {
        return new TollStorage();
    }

    public RedstoneTollGateTileEntity() {
        super(ModBlock.REDSTONE_TOLL_GATE_ENTITY_TYPE);
    }

    @Override
    public void tick() {
        assert level != null;
        if (!level.isClientSide) {
            BlockState state = this.getBlockState();
            if (!initialise){
                initialise = true;
                lastPowered = state.getValue(BlockStateProperties.POWERED);
            }
            if (lastPowered != state.getValue(BlockStateProperties.POWERED)) {
                startAllAnimation();
            }

            //block for gestion of animation
            if (animationOpeningInProcess()) {
                int animationStep = state.getValue(TollGate.ANIMATION);
                if (animationStep == 0) {
                    //add the sound of tollgate
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.TOLL_GATE_OPENING, 6.0F));
                }

                if (animationStep == 4) {
                    setBoolOpen(false);
                } else {
                    //add this condition to see the tollgate in opening process
                    //if (animationStep !=3) {
                    this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TollGate.ANIMATION, animationStep + 1));
                    //}
                }
            } else if (animationClosingInProcess()) {
                int animationStep = state.getValue(TollGate.ANIMATION);
                if (animationStep == 4) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.TOLL_GATE_CLOSING, 6.0F));
                }
                if (animationStep == 0) {
                    setBoolClose(false);
                } else {
                    this.level.setBlockAndUpdate(this.worldPosition, state.setValue(TollGate.ANIMATION, animationStep - 1));
                }
            }
            lastPowered = state.getValue(BlockStateProperties.POWERED);
        }
    }

    //start all animation of all the block that make the tollgate

    private void startAllAnimation(){
        startAnimation();
        if (!(this.getBlockState().getBlock() instanceof RedstoneTollGate))return;
        RedstoneTollGate tollGate = (RedstoneTollGate) this.getBlockState().getBlock();
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        TollGPosition tgp = this.getBlockState().getValue(TollGate.TG_POSITION);
        DoorHingeSide dhs = this.getBlockState().getValue(BlockStateProperties.DOOR_HINGE);
        List<BlockPos> posList = tollGate.getPositionOfBlockConnected(direction,tgp,dhs,this.worldPosition);
        for (BlockPos pos1 : posList){
            assert level != null;
            if (!(level.getBlockEntity(pos1) instanceof RedstoneTollGateTileEntity)) {
                throw new IllegalArgumentException("No tile entity on this blockPos :"+pos1);
            }
            //System.out.println("position of animated block :"+pos1);
            RedstoneTollGateTileEntity rtgte2 = (RedstoneTollGateTileEntity) level.getBlockEntity(pos1);
            assert rtgte2 != null;
            rtgte2.startAnimation();
        }
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

    //check if this TE is the control unit tile entity to avoid multiple definition of toll-storage that will be of no use
    public boolean isRightTE() {
        BlockState state = getBlockState();
        return state.getValue(TollGate.TG_POSITION) == TollGPosition.CONTROL_UNIT;
    }

    @Override
    public void load(BlockState state,CompoundNBT compound) {
        boolean isRightTE = compound.getBoolean("isCU");
        if (isRightTE) {
            CompoundNBT storage_tag = compound.getCompound("storage");
            getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(s -> ((INBTSerializable<CompoundNBT>) s).deserializeNBT(storage_tag)); }
        super.load(state,compound);
    }



    private boolean canWrite(){
        if (level == null){
            return true;
        }else {
            return isRightTE();
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        if (canWrite()) {
            getCapability(TollStorageCapability.TOLL_STORAGE).ifPresent(storage -> {
                CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>) storage).serializeNBT();
                tag.put("storage", compoundNBT);
            });
        }
        if (level != null){
            tag.putBoolean("isCU",isRightTE());
        }else{
            tag.putBoolean("isCU",false);
        }
        return super.save(tag);
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
