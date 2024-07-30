package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.animationboolean.AnimationBoolean;

import fr.mattmouss.gates.animationboolean.AnimationBooleanCapability;
import fr.mattmouss.gates.animationboolean.IAnimationBoolean;
import fr.mattmouss.gates.doors.GarageDoor;
import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.enum_door.Placing;
import fr.mattmouss.gates.setup.ModSound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;

public class GarageTileEntity extends BlockEntity {

    private final LazyOptional<AnimationBoolean> startAnimation = LazyOptional.of(this::getAnimation).cast();

    public GarageTileEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlock.GARAGE_TILE_TYPE,blockPos,blockState);
    }

    private AnimationBoolean getAnimation(){
        return new AnimationBoolean();
    }


    public void tick(Level level) {
        if (!level.isClientSide) {
            BlockState state = this.getBlockState();
            if (animationOpeningInProcess()) {
                int animationStep = state.getValue(GarageDoor.ANIMATION);
                if (animationStep == 0){
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSound.ANIMATION_GARAGE,1.0F));
                }
                if (animationStep == 5) {
                    setBoolOpen(false);
                } else {
                    level.setBlockAndUpdate(this.worldPosition, state.setValue(GarageDoor.ANIMATION, animationStep + 1));
                }
            } else if (animationClosingInProcess()) {
                int animationStep = state.getValue(GarageDoor.ANIMATION);
                if (animationStep == 5) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSound.ANIMATION_GARAGE, 1.0F));
                }
                if (animationStep == 0) {
                    setBoolClose(false);
                } else {
                    level.setBlockAndUpdate(this.worldPosition, state.setValue(GarageDoor.ANIMATION, animationStep - 1));
                }
            }
        }
    }


    public void startAnimation(){
        BlockState state = this.getBlockState();
        int animationStep = state.getValue(GarageDoor.ANIMATION);
        if (animationStep == 0) {
            setBoolOpen(true); //start opening animation
            //System.out.println("starting animation open");
        }else if (animationStep == 5){
            setBoolClose(true); //start closing animation
            //System.out.println("starting animation close");
        }
    }

    private boolean animationOpeningInProcess(){
        return getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).map(IAnimationBoolean::isOpening).orElse(false);
    }

    private boolean animationClosingInProcess(){
        return getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).map(IAnimationBoolean::isClosing).orElse(false);
    }

    private void setBoolOpen(Boolean bool){
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> animationBoolean.setBoolOpen(bool));
    }

    private void setBoolClose(Boolean bool){
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> animationBoolean.setBoolClose(bool));
    }

    @Override
    public void load(CompoundTag compound) {
        CompoundTag switch_tag = compound.getCompound("anim");
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> ((INBTSerializable<CompoundTag>)animationBoolean).deserializeNBT(switch_tag));
        super.load(compound);

    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> {
            CompoundTag compoundNBT = animationBoolean.serializeNBT();
            tag.put("anim",compoundNBT);
        });
        return super.save(tag);
    }


    public List<BlockPos> getPositionOfBlockConnected(){
        BlockState state = this.getBlockState();
        List<BlockPos> posList = new ArrayList<>();
        Direction dir_facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Placing placing = state.getValue(GarageDoor.GARAGE_PLACING);
        Direction dir_left_section = dir_facing.getClockWise();
        switch (placing){
            case DOWN_LEFT://block below in left
                //above position
                posList.add(worldPosition.above());
                //back blocks
                posList.add(worldPosition.above().relative(dir_facing.getOpposite()));
                posList.add(worldPosition.above()
                        .relative(dir_facing.getOpposite())
                        .relative(dir_left_section.getOpposite()));
                //right blocks
                posList.add(worldPosition.relative(dir_left_section.getOpposite()));
                posList.add(worldPosition.relative(dir_left_section.getOpposite()).above());
                break;
            case DOWN_RIGHT://block below in right
                //above block
                posList.add(worldPosition.above());
                //back blocks
                posList.add(worldPosition.above().relative(dir_facing.getOpposite()));
                posList.add(worldPosition.above()
                        .relative(dir_facing.getOpposite())
                        .relative(dir_left_section));
                //left blocks
                posList.add(worldPosition.relative(dir_left_section));
                posList.add(worldPosition.relative(dir_left_section).above());
                break;
            case UP_LEFT://block above in left
                //below block
                posList.add(worldPosition.below());
                //back blocks
                posList.add(worldPosition.relative(dir_facing.getOpposite()));
                posList.add(worldPosition
                        .relative(dir_facing.getOpposite())
                        .relative(dir_left_section.getOpposite()));
                //right blocks
                posList.add(worldPosition.relative(dir_left_section.getOpposite()));
                posList.add(worldPosition.relative(dir_left_section.getOpposite()).below());
                break;
            case UP_RIGHT://block above in right
                //below block
                posList.add(worldPosition.below());
                //back blocks
                posList.add(worldPosition.relative(dir_facing.getOpposite()));
                posList.add(worldPosition.relative(dir_facing.getOpposite()).relative(dir_left_section));
                //left blocks
                posList.add(worldPosition.relative(dir_left_section));
                posList.add(worldPosition.relative(dir_left_section).below());
                break;
            case BACK_LEFT://block on the back left
                //right block
                posList.add(worldPosition.relative(dir_left_section.getOpposite()));
                //position in front left
                posList.add(worldPosition.relative(dir_facing));
                posList.add(worldPosition.relative(dir_facing).below());
                //position in front right
                posList.add(worldPosition
                        .relative(dir_facing)
                        .relative(dir_left_section.getOpposite()));
                posList.add(worldPosition
                        .relative(dir_facing)
                        .relative(dir_left_section.getOpposite())
                        .below());
                break;
            case BACK_RIGHT://block on the back right
                //left block
                posList.add(worldPosition.relative(dir_left_section));
                //position in front right
                posList.add(worldPosition.relative(dir_facing));
                posList.add(worldPosition.relative(dir_facing).below());
                //position in front left
                posList.add(worldPosition
                        .relative(dir_facing)
                        .relative(dir_left_section));
                posList.add(worldPosition
                        .relative(dir_facing)
                        .relative(dir_left_section)
                        .below());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + placing);
        }
        return posList;

    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY){
            return startAnimation.cast();
        }
        return super.getCapability(cap, side);
    }


}
