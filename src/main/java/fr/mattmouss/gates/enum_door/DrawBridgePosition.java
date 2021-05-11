package fr.mattmouss.gates.enum_door;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;


public enum DrawBridgePosition implements IStringSerializable {
    DOOR_LEFT_UP(0,"door_left_up"),
    DOOR_RIGHT_UP(1,"door_right_up"),
    DOOR_LEFT_UUP(2,"door_left_up_up"),
    DOOR_RIGHT_UUP(3,"door_right_up_up"),
    DOOR_LEFT_DOWN(4,"door_left_down"),
    DOOR_RIGHT_DOWN(5,"door_right_down"),
    BRIDGE_LEFT(6,"bridge_left"),
    BRIDGE_RIGHT(7,"bridge_right"),
    BRIDGE_EXT_LEFT(8,"bridge_ext_left"),
    BRIDGE_EXT_RIGHT(9,"bridge_ext_right");

    int meta;
    String id;
    DrawBridgePosition(int meta,String id){
        this.meta=meta;
        this.id=id;
    }

    public static DrawBridgePosition[] getNonBridgePositions(){
        return new DrawBridgePosition[]{DOOR_LEFT_UP,DOOR_RIGHT_UP,DOOR_LEFT_UUP,DOOR_RIGHT_UUP,DOOR_LEFT_DOWN,DOOR_RIGHT_DOWN};
    }

    public int getMeta() {
        return meta;
    }

    public boolean isRight(){
        return meta % 2 ==1;
    }

    public boolean isUp(){
        return meta < 4;
    }

    public boolean isUpUp(){return meta>1 && isUp(); }

    public boolean isBridge(){
        return meta > 5;
    }

    public boolean isBridgeExt() { return meta > 7;}

    @Override
    public String getName() {
        return id;
    }
    //how to get position of a block based on leftDownPos
    public BlockPos getOffsetPos(BlockPos leftDownPos, Direction facing){
        BlockPos finalPos = new BlockPos(leftDownPos);
        //add up function
        if (isUp()){
            finalPos=finalPos.up();
        }
        //add an additionnal up function
        if (isUpUp()){
            finalPos=finalPos.up();
        }
        //add offset on right
        if (isRight()){
            finalPos=finalPos.offset(facing.rotateY());
        }
        //add offset on deepness
        if (isBridge()){
            finalPos=finalPos.offset(facing);
        }
        //add an additional offset on deepness
        if (isBridgeExt()){
            finalPos=finalPos.offset(facing);
        }
        return finalPos;
    }

    //how to get to the position of the left Down Pos from our pos
    public BlockPos getCounterOffsetPos(BlockPos ourPos, Direction facing){
        BlockPos finalPos = new BlockPos(ourPos);
        //add down function
        if (isUp()){
            finalPos=finalPos.down();
        }
        //add an additional down function
        if (isUpUp()){
            finalPos=finalPos.down();
        }
        //add offset on left
        if (isRight()){
            finalPos=finalPos.offset(facing.rotateYCCW());
        }
        //add offset on deepness
        if (isBridge()){
            finalPos=finalPos.offset(facing.getOpposite());
        }
        //add an additional offset on deepness
        if (isBridgeExt()){
            finalPos=finalPos.offset(facing.getOpposite());
        }
        return finalPos;
    }

    public boolean isInnerUpdate(Direction facingUpdate,Direction blockFacing){
        return (isLateralUpdate(facingUpdate,blockFacing) || //right or left part is destroyed or powered
                (isBridge() && !isBridgeExt()) && (facingUpdate.getAxis() == blockFacing.getAxis()) || //when bridge but not extreme there is both facing and opposite update
                (isBridgeExt() && facingUpdate == blockFacing.getOpposite()) || //when is extreme bridge only facing opposite update is important
                (isUp() && !isUpUp()) && (facingUpdate.getAxis() == Direction.Axis.Y) || //when is up but not extreme up there is two update from both up and down
                (isUpUp() && facingUpdate == Direction.DOWN) || //when is extreme up only down update is important
                (!isUp() && !isBridge()) && (facingUpdate == Direction.UP || facingUpdate==blockFacing)); //when is down take care of up and bridge update
    }

    private boolean isLateralUpdate(Direction facingUpdate,Direction blockFacing){
        return (isRight() && (facingUpdate == blockFacing.rotateYCCW()) ) ||
                (!isRight() && (facingUpdate == blockFacing.rotateY()));
    }

}
