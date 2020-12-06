package fr.mattmouss.gates.enum_door;

import net.minecraft.util.IStringSerializable;

public enum DoorPlacing implements IStringSerializable {
    LEFT_UP(0,"left_up"),
    RIGHT_UP(1,"right_up"),
    CENTER_UP(2,"center_up"),
    LEFT_DOWN(3,"left_down"),
    RIGHT_DOWN(4,"right_down"),
    CENTER_DOWN(5,"center_down");
    int meta;
    String name;
    DoorPlacing(int meta, String name){
        this.meta = meta;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isSide() {
        return meta % 3 != 2; // is not center place
    }

    public boolean isUp(){
        return meta<3;
    }
    public boolean isLeft(){
        return meta % 3 == 0;
    }
}
