package fr.mattmouss.gates.enum_door;

import net.minecraft.util.IStringSerializable;

public enum Placing implements IStringSerializable {
    DOWN_LEFT(0,"down_left"),
    DOWN_RIGHT(1,"down_right"),
    UP_LEFT(2,"up_left"),
    UP_RIGHT(3,"up_right"),
    BACK_LEFT(4,"back_left"),
    BACK_RIGHT(5,"back_right");

    private final int meta;
    private final String name;

    Placing(int meta, String name){
        this.meta=meta;
        this.name=name;
    }

    public int getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public boolean isUp(){
        return meta > 1;
    }

    public boolean isRight(){
        return (meta % 2 ==1);
    }


}
