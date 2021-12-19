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

    public boolean isEmpty(int anim_stade){
        //si on est a l'emplacement central ou extrême lorsque la barre est levée on a un block empty
        //si on est à l'emplacement au dessus lorsque la barrière est fermé on a un block empty
        return (((meta==0 || meta==2)&& anim_stade>2 )|| (meta ==3 && anim_stade<3));
    }

    public boolean isSimpleBarrier(int anim_stade){
        //si on est à l'emplacement central ou extrême losrque la barre est baissée on a un block de barrière classique
        //si on est à l'emplacement au dessus lorsque la barrière est levée on a un block de bariière classique
        return (anim_stade == 0 && (meta == 2 || meta == 0)) || (anim_stade == 4 && meta==3);
    }

    public boolean isDownBlock(){
        return  meta !=3;
    }




}
