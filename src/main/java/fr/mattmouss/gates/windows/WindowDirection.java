package fr.mattmouss.gates.windows;

import net.minecraft.util.Direction;

public class WindowDirection {
    private int[] dir = new int[6];

    public WindowDirection(int nb_offset,Direction dir){
        int index = dir.getIndex();
        this.dir[index] = nb_offset;
    }

    public WindowDirection(int nb_offset,Direction dir,int nb_offset2,Direction dir2){
        int index = dir.getIndex();
        this.dir[index] = nb_offset;
        int index2 = dir2.getIndex();
        this.dir[index2] = nb_offset2;
    }

    public int[] getDirections() {
        return dir;
    }
}
