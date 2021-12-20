package fr.mattmouss.gates.windows;

import fr.mattmouss.gates.util.ExtendDirection;

public class WindowDirection {
    private final int[] dir = new int[10];

    public WindowDirection(int nb_offset, ExtendDirection dir){
        int index = dir.getIndex();
        this.dir[index] = nb_offset;
}

    public WindowDirection(int nb_offset,ExtendDirection dir,int nb_offset2,ExtendDirection dir2){
        int index = dir.getIndex();
        this.dir[index] = nb_offset;
        int index2 = dir2.getIndex();
        this.dir[index2] = nb_offset2;
    }

    public int[] getDirections() {
        return dir;
    }
}
