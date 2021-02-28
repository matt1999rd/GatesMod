package fr.mattmouss.gates.voxels;

import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;

public class VoxelDefinition {
    public static VoxelShape[] largeDoorCircleShape = new VoxelShape[48];
    public static VoxelShape[] largeDoorSquareShape = new VoxelShape[48];
    public static boolean isInit = false;
    public static void init() {
        isInit = true;
        int j=0;
        for (DoorPlacing placing : DoorPlacing.getPlacingForLargeDoor()){
            for (int i=0;i<4;i++){
                largeDoorCircleShape[8*j+2*i]=
                        Functions.makeCircleShape(placing,
                                Direction.byHorizontalIndex(i),
                                false);
                largeDoorCircleShape[8*j+2*i+1]=
                        Functions.makeCircleShape(placing,
                                Direction.byHorizontalIndex(i),
                                true);
                largeDoorSquareShape[8*j+2*i]=
                        Functions.makeSquareShape(placing,
                                Direction.byHorizontalIndex(i),
                                false);
                largeDoorSquareShape[8*j+2*i+1]=
                        Functions.makeSquareShape(placing,
                                Direction.byHorizontalIndex(i),
                                true);
            }
            j++;
        }
    }

}
