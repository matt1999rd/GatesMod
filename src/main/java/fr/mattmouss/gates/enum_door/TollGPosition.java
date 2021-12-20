package fr.mattmouss.gates.enum_door;

import net.minecraft.util.IStringSerializable;

public enum TollGPosition implements IStringSerializable {
    MAIN(0,"main"),
    EMPTY_BASE(1,"emptyb"),
    EMPTY_EXT(2,"emptye"),
    UP_BLOCK(3,"up"),
    CONTROL_UNIT(4,"ctrl_unit");

    private final int meta;
    private final String name;

    TollGPosition(int meta,String name) {
        this.meta = meta;
        this.name = name;
    }
    public int getMeta() {
        return meta;
    }

    public String getSerializedName() {
        return name;
    }

    public boolean isEmpty(int anim_state){
        //if it is opened, then empty block are main and empty ext (main is the middle part of the red/white part)
        //if it is closed, then empty block is up block
        return (((meta==0 || meta==2)&& anim_state>2 )|| (meta ==3 && anim_state<3));
    }

    public boolean isSimpleBarrier(int anim_state){
        //if it is opened, then simple barrier block is up block
        //if it is closed, then simple barrier block are main and empty ext (main is the middle part of the red/white part)
        return (anim_state == 0 && (meta == 2 || meta == 0)) || (anim_state == 4 && meta==3);
    }





}
