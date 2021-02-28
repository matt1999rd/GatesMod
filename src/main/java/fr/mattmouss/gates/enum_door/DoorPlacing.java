package fr.mattmouss.gates.enum_door;

import com.google.common.collect.Lists;
import net.minecraft.util.IStringSerializable;

import java.util.List;

public enum DoorPlacing implements IStringSerializable {
    LEFT_UP(0,"left_up"),
    RIGHT_UP(1,"right_up"),
    LEFT_DOWN(2,"left_down"),
    RIGHT_DOWN(3,"right_down"),
    LEFT_CENTER(4,"left_center"),
    RIGHT_CENTER(5,"right_center"),
    CENTER_UP(6,"center_up"),
    CENTER_DOWN(7,"center_down");

    int meta;
    String name;
    DoorPlacing(int meta, String name){
        this.meta = meta;
        this.name = name;
    }

    public int getMeta() {
        return meta;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isSide() {
        return meta<=5; // is not center place
    }

    public boolean isUp(){
        return meta<2 || meta == 6;
    }

    public boolean isCenterY(){
        return meta >3 && meta<6;
    }

    public boolean isDown(){return !isCenterY() && !isUp();}

    public boolean isLeft(){
        return meta % 2 == 0 && meta<5;
    }

    public boolean hasRightNeighbor(){return isLeft() || !isSide();}

    public boolean hasLeftNeighbor(){return !isLeft();}

    public static List<DoorPlacing> getPlacingForLargeDoor(){
        List<DoorPlacing> placings = Lists.newArrayList(DoorPlacing.values());
        placings.remove(DoorPlacing.CENTER_DOWN);
        placings.remove(DoorPlacing.CENTER_UP);
        return placings;
    }

}
