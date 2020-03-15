package fr.mattmouss.gates.enum_door;

import net.minecraft.util.IStringSerializable;

public enum TurnSPosition implements IStringSerializable {
    MAIN(0,"main"),
    RIGHT_BLOCK(1,"right"),
    LEFT_BLOCK(2,"left");

    private final int meta;
    private final String name;
    TurnSPosition(int meta_in,String name_in){
        meta = meta_in;
        name = name_in;
    }

    public String getName() {
        return name;
    }

    public int getMeta() {
        return meta;
    }
}
