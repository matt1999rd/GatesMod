package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.animationboolean.AnimationBoolean;

import fr.mattmouss.gates.animationboolean.AnimationBooleanCapability;
import fr.mattmouss.gates.animationboolean.IAnimationBoolean;
import fr.mattmouss.gates.doors.GarageDoor;
import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.enum_door.Placing;
import fr.mattmouss.gates.setup.ModSound;
import net.minecraft.block.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GarageTileEntity extends TileEntity implements ITickableTileEntity {
    public GarageTileEntity() {
        super(ModBlock.GARAGE_TILE_TYPE);
    }

    private LazyOptional<AnimationBoolean> startAnimation = LazyOptional.of(this::getAnimation).cast();

    private AnimationBoolean getAnimation(){
        return new AnimationBoolean();
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            BlockState state = this.getBlockState();
            if (animationOpeningInProcess()) {
                int animationStep = state.getValue(GarageDoor.ANIMATION);
                if (animationStep == 0){
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.ANIMATION_GARAGE,1.0F));
                }
                if (animationStep == 5) {
                    setBoolOpen(false);
                } else {
                    this.level.setBlockAndUpdate(this.worldPosition, state.setValue(GarageDoor.ANIMATION, animationStep + 1));
                }
            } else if (animationClosingInProcess()) {
                int animationStep = state.getValue(GarageDoor.ANIMATION);
                if (animationStep == 5) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.ANIMATION_GARAGE, 1.0F));
                }
                if (animationStep == 0) {
                    setBoolClose(false);
                } else {
                    this.level.setBlockAndUpdate(this.worldPosition, state.setValue(GarageDoor.ANIMATION, animationStep - 1));
                }
            }
            //checkStability();
            //checkRedstoneNearby();
        }
    }


    public void startAnimation(){
        BlockState state = this.getBlockState();
        int animationStep = state.getValue(GarageDoor.ANIMATION);
        if (animationStep == 0) {
            setBoolOpen(true); //mettre en route l'animation d'ouverture
            //System.out.println("starting animation open");
        }else if (animationStep == 5){
            setBoolClose(true); //mettre en route l'animation de fermeture
            //System.out.println("starting animation close");
        }
    }

    public void startAnimation(boolean opening){
        BlockState state = this.getBlockState();
        int animationStep = state.getValue(GarageDoor.ANIMATION);
        if (animationStep == 0 && opening) {
            setBoolOpen(true); //mettre en route l'animation d'ouverture
            //System.out.println("starting animation open");
        }else if (animationStep == 5 && !opening){
            setBoolClose(true); //mettre en route l'animation de fermeture
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
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean ->{
            animationBoolean.setBoolOpen(bool);
        });
    }

    private void setBoolClose(Boolean bool){
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> {
            animationBoolean.setBoolClose(bool);
        });
    }

    @Override
    public void load(BlockState state,CompoundNBT compound) {
        CompoundNBT switch_tag = compound.getCompound("anim");
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> ((INBTSerializable<CompoundNBT>)animationBoolean).deserializeNBT(switch_tag));
        super.load(state,compound);

    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)animationBoolean).serializeNBT();
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
            case DOWN_LEFT://block en bas à gauche
                //position au dessus
                posList.add(worldPosition.above());
                //blocks de l'arrière
                posList.add(worldPosition.above().relative(dir_facing.getOpposite()));
                posList.add(worldPosition.above()
                        .relative(dir_facing.getOpposite())
                        .relative(dir_left_section.getOpposite()));
                //block à droite
                posList.add(worldPosition.relative(dir_left_section.getOpposite()));
                posList.add(worldPosition.relative(dir_left_section.getOpposite()).above());
                break;
            case DOWN_RIGHT://block en bas à droite
                //position au dessus
                posList.add(worldPosition.above());
                //blocks de l'arrière
                posList.add(worldPosition.above().relative(dir_facing.getOpposite()));
                posList.add(worldPosition.above()
                        .relative(dir_facing.getOpposite())
                        .relative(dir_left_section));
                //block à gauche
                posList.add(worldPosition.relative(dir_left_section));
                posList.add(worldPosition.relative(dir_left_section).above());
                break;
            case UP_LEFT://block en haut à gauche
                //position au dessous
                posList.add(worldPosition.below());
                //blocks de l'arrière
                posList.add(worldPosition.relative(dir_facing.getOpposite()));
                posList.add(worldPosition
                        .relative(dir_facing.getOpposite())
                        .relative(dir_left_section.getOpposite()));
                //block à droite
                posList.add(worldPosition.relative(dir_left_section.getOpposite()));
                posList.add(worldPosition.relative(dir_left_section.getOpposite()).below());
                break;
            case UP_RIGHT://block en haut à droite
                //position au dessous
                posList.add(worldPosition.below());
                //blocks de l'arrière
                posList.add(worldPosition.relative(dir_facing.getOpposite()));
                posList.add(worldPosition.relative(dir_facing.getOpposite()).relative(dir_left_section));
                //block à gauche
                posList.add(worldPosition.relative(dir_left_section));
                posList.add(worldPosition.relative(dir_left_section).below());
                break;
            case BACK_LEFT://block derrière à gauche
                //position à droite
                posList.add(worldPosition.relative(dir_left_section.getOpposite()));
                //position devant en haut et en bas
                posList.add(worldPosition.relative(dir_facing));
                posList.add(worldPosition.relative(dir_facing).below());
                //position devant à droite
                posList.add(worldPosition
                        .relative(dir_facing)
                        .relative(dir_left_section.getOpposite()));
                posList.add(worldPosition
                        .relative(dir_facing)
                        .relative(dir_left_section.getOpposite())
                        .below());
                break;
            case BACK_RIGHT://block derrière à droite
                //position à gauche
                posList.add(worldPosition.relative(dir_left_section));
                //position devant en haut et en bas
                posList.add(worldPosition.relative(dir_facing));
                posList.add(worldPosition.relative(dir_facing).below());
                //position devant à gauche
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

    public void checkRedstoneNearby(){
        List<BlockPos> commonGarageBlock = getPositionOfBlockConnected();
        Boolean isPowered = false;
        for(int i = 0; i<6;i++){
            if (!commonGarageBlock.contains(worldPosition.relative(Direction.from3DDataValue(i))) && isBlockPowered(worldPosition.relative(Direction.from3DDataValue(i)))){
                isPowered = true;
            }
            if (!commonGarageBlock.contains(worldPosition.relative(Direction.from3DDataValue(i)))){
                System.out.println("le block à la position "+
                        worldPosition.relative(Direction.from3DDataValue(i))
                        +"est il powered ? R :"
                        + isBlockPowered(worldPosition.relative(Direction.from3DDataValue(i))));
            }
        }
        System.out.println("is Powered :"+isPowered);

        //on démarre l'animation ouverture si alimenté et à 0 ou fermeture si non alimnté et à 5
        this.startAnimation(isPowered);
        for (BlockPos pos : commonGarageBlock){
            ((GarageTileEntity)level.getBlockEntity(pos)).startAnimation(isPowered);
        }
    }

    private boolean isBlockPowered(BlockPos offset) {
        BlockState state =level.getBlockState(offset);
        Boolean res = false;
        if (state.hasProperty(BlockStateProperties.POWERED)){
            res = state.getValue(BlockStateProperties.POWERED);
        }
        if (state.hasProperty(BlockStateProperties.POWER)){
            res = state.getValue(BlockStateProperties.POWER)>10;
        }

        return res;
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
