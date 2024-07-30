package fr.mattmouss.gates.voxels;

import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.enum_door.DrawBridgePosition;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelDefinition {
    public static VoxelShape[] largeDoorCircleShape = new VoxelShape[48];
    public static VoxelShape[] largeDoorSquareShape = new VoxelShape[48];
    public static VoxelShape[] gardenDoorShape = new VoxelShape[32];
    public static VoxelShape[] drawBridgeShape = new VoxelShape[120];
    public static boolean isInit = false;
    public static void init() {
        isInit = true;
        int j=0;
        for (DoorPlacing placing : DoorPlacing.getPlacingForLargeDoor()){
            for (int i=0;i<4;i++){
                largeDoorCircleShape[8*j+2*i]=
                        Functions.makeCircleShape(placing,
                                Direction.from2DDataValue(i),
                                false);
                largeDoorCircleShape[8*j+2*i+1]=
                        Functions.makeCircleShape(placing,
                                Direction.from2DDataValue(i),
                                true);
                largeDoorSquareShape[8*j+2*i]=
                        Functions.makeSquareShape(placing,
                                Direction.from2DDataValue(i),
                                false);
                largeDoorSquareShape[8*j+2*i+1]=
                        Functions.makeSquareShape(placing,
                                Direction.from2DDataValue(i),
                                true);
            }
            j++;
        }
        j=0;
        for (DoorPlacing placing : DoorPlacing.getPlacingForGardenDoor()){
            for (int i=0;i<4;i++){
                gardenDoorShape[8*j+2*i]=
                        Functions.makeGardenDoorShape(placing,
                                Direction.from2DDataValue(i),
                                false);
                gardenDoorShape[8*j+2*i+1]=
                        Functions.makeGardenDoorShape(placing,
                                Direction.from2DDataValue(i),
                                true);
            }
            j++;
        }
        j=0;
        for (DrawBridgePosition position : DrawBridgePosition.getNonBridgePositions()){
            //for animation
            for (int animState=0;animState<5;animState++){
                //for direction
                for (int k=0;k<4;k++){
                    drawBridgeShape[20*j+4*animState+k]=
                            Functions.makeDrawBridgeShape(position,
                                    animState,
                                    Direction.from2DDataValue(k));
                }
            }
            j++;
        }
    }

}
