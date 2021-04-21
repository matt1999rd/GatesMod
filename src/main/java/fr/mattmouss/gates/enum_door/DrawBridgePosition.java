package fr.mattmouss.gates.enum_door;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;


public enum DrawBridgePosition implements IStringSerializable {
    DOOR_LEFT_UP(0,"door_left_up"),
    DOOR_RIGHT_UP(1,"door_right_up"),
    DOOR_LEFT_DOWN(2,"door_left_down"),
    DOOR_RIGHT_DOWN(3,"door_right_down"),
    BRIDGE_LEFT(4,"bridge_left"),
    BRIDGE_RIGHT(5,"bridge_right");

    int meta;
    String id;
    DrawBridgePosition(int meta,String id){
        this.meta=meta;
        this.id=id;
    }

    public static DrawBridgePosition[] getNonBridgePositions(){
        return new DrawBridgePosition[]{DOOR_LEFT_UP,DOOR_RIGHT_UP,DOOR_LEFT_DOWN,DOOR_RIGHT_DOWN};
    }

    public int getMeta() {
        return meta;
    }

    public boolean isRight(){
        return meta % 2 ==1;
    }

    public boolean isUp(){
        return meta < 2;
    }

    public boolean isBridge(){
        return meta > 3;
    }

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
        //add offset on right
        if (isRight()){
            finalPos=finalPos.offset(facing.rotateY());
        }
        //add offset on deepNess
        if (isBridge()){
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
        //add offset on left
        if (isRight()){
            finalPos=finalPos.offset(facing.rotateYCCW());
        }
        //add offset on deepNess
        if (isBridge()){
            finalPos=finalPos.offset(facing.getOpposite());
        }
        return finalPos;
    }

    public boolean isInnerUpdate(Direction facingUpdate,Direction blockFacing){
        return (isLateralUpdate(facingUpdate,blockFacing) ||
                isBridge() && facingUpdate == blockFacing.getOpposite()||
                isUp() && facingUpdate == Direction.DOWN ||
                (!isUp() && !isBridge()) && (facingUpdate == Direction.UP || facingUpdate==blockFacing));
    }

    private boolean isLateralUpdate(Direction facingUpdate,Direction blockFacing){
        return (isRight() && (facingUpdate == blockFacing.rotateYCCW()) ) ||
                (!isRight() && (facingUpdate == blockFacing.rotateY()));
    }

}
