package fr.mattmouss.gates.enum_door;

import net.minecraft.util.IStringSerializable;

public enum TurnSPosition implements IStringSerializable {
    MAIN(0,"main"),
    RIGHT_BLOCK(1,"right"),
    LEFT_BLOCK(2,"left"),
    UP_BLOCK(3,"up");

    private final int meta;
    private final String name;
    TurnSPosition(int meta_in,String name_in){
        meta = meta_in;
        name = name_in;
    }

    public String getSerializedName() {
        return name;
    }

    public int getMeta() {
        return meta;
    }

    public boolean isSolid() {
        return this == RIGHT_BLOCK || this == LEFT_BLOCK;
    }

    public boolean isDown(){ return this != UP_BLOCK;}
}
