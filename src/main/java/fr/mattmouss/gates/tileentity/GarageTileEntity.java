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
        if (!world.isRemote) {
            BlockState state = this.getBlockState();
            if (animationOpeningInProcess()) {
                int animationStep = state.get(GarageDoor.ANIMATION);
                if (animationStep == 0){
                    Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(ModSound.ANIMATION_GARAGE,1.0F));
                }
                if (animationStep == 5) {
                    setBoolOpen(false);
                } else {
                    this.world.setBlockState(this.pos, state.with(GarageDoor.ANIMATION, animationStep + 1));
                }
            } else if (animationClosingInProcess()) {
                int animationStep = state.get(GarageDoor.ANIMATION);
                if (animationStep == 5) {
                    Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(ModSound.ANIMATION_GARAGE, 1.0F));
                }
                if (animationStep == 0) {
                    setBoolClose(false);
                } else {
                    this.world.setBlockState(this.pos, state.with(GarageDoor.ANIMATION, animationStep - 1));
                }
            } else {
                checkStability();
                //checkRedstoneNearby();
            }
        }

    }

    private void checkStability() {
        Placing placing = this.getBlockState().get(GarageDoor.GARAGE_PLACING);
        Direction dir_left_section = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING).rotateY();
        //test entre droite et gauche
        //droite si à droite et gauche si à gauche
        Direction placing_offset = (placing.isRight())?dir_left_section.getOpposite():dir_left_section;
        BlockPos pos_lat_supp = pos.offset(placing_offset);
        BlockState lat_block_state = world.getBlockState(pos_lat_supp);
        String pos_str = (placing.isRight())?"droite":"gauche";
        //block à droite ou à gauche qui doit être présent et stable
        if (!lat_block_state.getMaterial().blocksMovement()){
            System.out.println("pas de block à "+pos_str+" : destruction du block !!");
            destroyBlock();
            //return pour arrêter la fonction
            return;
        }

        //test entre haut et bas
        //haut si en haut et bas si en bas
        placing_offset = (placing.isUp())?Direction.UP:Direction.DOWN;
        BlockPos pos_y_supp= pos.offset(placing_offset);
        Block y_block = world.getBlockState(pos_y_supp).getBlock();
        String pos_str_y = (placing.isUp())?"au dessus":"en dessous";
        //block en haut ou en bas qui doit être présent et stable
        if (y_block instanceof AirBlock || y_block instanceof BushBlock){
            System.out.println("pas de block "+pos_str_y+" : destruction du block !!");
            destroyBlock();
            //return pour arrêter la fonction
            return;
        }
        //System.out.println("stability checked for block at position :"+placing.getName());

    }

    private void destroyBlock() {
        GarageDoor door = (GarageDoor) this.getBlockState().getBlock();
        List<BlockPos> posList = getPositionOfBlockConnected();
        door.deleteBlock(pos,world);
        posList.forEach(pos_neighbor-> {
            Block block = world.getBlockState(pos_neighbor).getBlock();
            if (block instanceof GarageDoor){
                GarageDoor neighborDoor = (GarageDoor)world.getBlockState(pos_neighbor).getBlock();
                neighborDoor.deleteBlock(pos_neighbor,world);
            }
        });
    }


    public void startAnimation(){
        BlockState state = this.getBlockState();
        int animationStep = state.get(GarageDoor.ANIMATION);
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
        int animationStep = state.get(GarageDoor.ANIMATION);
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
    public void read(CompoundNBT compound) {
        CompoundNBT switch_tag = compound.getCompound("anim");
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> ((INBTSerializable<CompoundNBT>)animationBoolean).deserializeNBT(switch_tag));
        super.read(compound);

    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        getCapability(AnimationBooleanCapability.ANIMATION_BOOLEAN_CAPABILITY).ifPresent(animationBoolean -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)animationBoolean).serializeNBT();
            tag.put("anim",compoundNBT);
        });
        return super.write(tag);
    }


    public List<BlockPos> getPositionOfBlockConnected(){
        BlockState state = this.getBlockState();
        List<BlockPos> posList = new ArrayList<>();
        Direction dir_facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Placing placing = state.get(GarageDoor.GARAGE_PLACING);
        Direction dir_left_section = dir_facing.rotateY();
        switch (placing){
            case DOWN_LEFT://block en bas à gauche
                //position au dessus
                posList.add(pos.up());
                //blocks de l'arrière
                posList.add(pos.up().offset(dir_facing.getOpposite()));
                posList.add(pos.up()
                        .offset(dir_facing.getOpposite())
                        .offset(dir_left_section.getOpposite()));
                //block à droite
                posList.add(pos.offset(dir_left_section.getOpposite()));
                posList.add(pos.offset(dir_left_section.getOpposite()).up());
                break;
            case DOWN_RIGHT://block en bas à droite
                //position au dessus
                posList.add(pos.up());
                //blocks de l'arrière
                posList.add(pos.up().offset(dir_facing.getOpposite()));
                posList.add(pos.up()
                        .offset(dir_facing.getOpposite())
                        .offset(dir_left_section));
                //block à gauche
                posList.add(pos.offset(dir_left_section));
                posList.add(pos.offset(dir_left_section).up());
                break;
            case UP_LEFT://block en haut à gauche
                //position au dessous
                posList.add(pos.down());
                //blocks de l'arrière
                posList.add(pos.offset(dir_facing.getOpposite()));
                posList.add(pos
                        .offset(dir_facing.getOpposite())
                        .offset(dir_left_section.getOpposite()));
                //block à droite
                posList.add(pos.offset(dir_left_section.getOpposite()));
                posList.add(pos.offset(dir_left_section.getOpposite()).down());
                break;
            case UP_RIGHT://block en haut à droite
                //position au dessous
                posList.add(pos.down());
                //blocks de l'arrière
                posList.add(pos.offset(dir_facing.getOpposite()));
                posList.add(pos.offset(dir_facing.getOpposite()).offset(dir_left_section));
                //block à gauche
                posList.add(pos.offset(dir_left_section));
                posList.add(pos.offset(dir_left_section).down());
                break;
            case BACK_LEFT://block derrière à gauche
                //position à droite
                posList.add(pos.offset(dir_left_section.getOpposite()));
                //position devant en haut et en bas
                posList.add(pos.offset(dir_facing));
                posList.add(pos.offset(dir_facing).down());
                //position devant à droite
                posList.add(pos
                        .offset(dir_facing)
                        .offset(dir_left_section.getOpposite()));
                posList.add(pos
                        .offset(dir_facing)
                        .offset(dir_left_section.getOpposite())
                        .down());
                break;
            case BACK_RIGHT://block derrière à droite
                //position à gauche
                posList.add(pos.offset(dir_left_section));
                //position devant en haut et en bas
                posList.add(pos.offset(dir_facing));
                posList.add(pos.offset(dir_facing).down());
                //position devant à gauche
                posList.add(pos
                        .offset(dir_facing)
                        .offset(dir_left_section));
                posList.add(pos
                        .offset(dir_facing)
                        .offset(dir_left_section)
                        .down());
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
            if (!commonGarageBlock.contains(pos.offset(Direction.byIndex(i))) && isBlockPowered(pos.offset(Direction.byIndex(i)))){
                isPowered = true;
            }
            if (!commonGarageBlock.contains(pos.offset(Direction.byIndex(i)))){
                System.out.println("le block à la position "+
                        pos.offset(Direction.byIndex(i))
                        +"est il powered ? R :"
                        + isBlockPowered(pos.offset(Direction.byIndex(i))));
            }
        }
        System.out.println("is Powered :"+isPowered);

        //on démarre l'animation ouverture si alimenté et à 0 ou fermeture si non alimnté et à 5
        this.startAnimation(isPowered);
        for (BlockPos pos : commonGarageBlock){
            ((GarageTileEntity)world.getTileEntity(pos)).startAnimation(isPowered);
        }
    }

    private boolean isBlockPowered(BlockPos offset) {
        BlockState state =world.getBlockState(offset);
        Boolean res = false;
        if (state.has(BlockStateProperties.POWERED)){
            res = state.get(BlockStateProperties.POWERED);
        }
        if (state.has(BlockStateProperties.POWER_0_15)){
            res = state.get(BlockStateProperties.POWER_0_15)>10;
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
