package fr.mattmouss.gates.voxels;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

//util class to simplify VoxelShape creation
public class VoxelDoubles {
    public static final VoxelDoubles EMPTY = new VoxelDoubles(0,0,0,0,0,0,false);
    public static final VoxelDoubles FULL = new VoxelDoubles(0,0,0,16,16,16,false);

    private final double[] planeVal;
    public VoxelDoubles(int x1, int y1, int z1, int x2, int y2, int z2, boolean isSizeUsed){
        if (isSizeUsed){
            //we build our voxel on size x y z for second argument
            planeVal = new double[]{x1, y1, z1, x1+x2, y1+y2, z1+z2};
            return;
        }
        //we build our voxel on origin and end of the cube
        planeVal = new double[]{x1, y1, z1, x2, y2, z2};
    }

    public VoxelDoubles(double x1, double y1, double z1, double x2, double y2, double z2, boolean isSizeUsed){
        if (isSizeUsed){
            //we build our voxel on size x y z for second argument
            planeVal = new double[]{x1, y1, z1, x1+x2, y1+y2, z1+z2};
            return;
        }
        //we build our voxel on origin and end of the cube
        planeVal = new double[]{x1, y1, z1, x2, y2, z2};
    }



    //rotate around given axis with angle of 90° * number_of_rotation counter clock wise
    public VoxelDoubles rotateCCW(int number_of_rotation, Direction.Axis axis){
        number_of_rotation = number_of_rotation%4;
        return this.rotateCW(4-number_of_rotation,axis);
    }

    //rotate around given axis with angle of 90° * number_of_rotation clock wise
    public VoxelDoubles rotateCW(int number_of_rotation, Direction.Axis axis){
        number_of_rotation = number_of_rotation%4;
        double x1 = planeVal[0];
        double y1 = planeVal[1];
        double z1 = planeVal[2];
        double x2 = planeVal[3];
        double y2 = planeVal[4];
        double z2 = planeVal[5];
        switch (number_of_rotation){
            case 0:
                return this;
            case 1:
                switch (axis){
                    case X:
                        return new VoxelDoubles(x1,16-z1,y1,x2,16-z2,y2,false);
                    case Y:
                        return new VoxelDoubles(16-z1,y1,x1,16-z2,y2,x2,false);
                    case Z:
                        return new VoxelDoubles(y1,16-x1,z1,y2,16-x2,z2,false);
                }
            case 2:
                return this.rotateCW(1,axis).rotateCW(1,axis);
            case 3:
                return this.rotateCW(2,axis).rotateCW(1,axis);
            default:
                throw new IllegalStateException("Unexpected value: " + number_of_rotation);
        }

    }

    public VoxelDoubles rotate(Direction present_direction, Direction changing_direction){
        if (present_direction == changing_direction){
            return this;
        }
        //if the two direction is on same axis, we can make a rotation of 180° over any of the two other axis
        if (present_direction.getAxis() == changing_direction.getAxis()){
            Direction.Axis other_axis = (present_direction.getAxis().isHorizontal() ? Direction.Axis.Y : Direction.Axis.X);
            return this.rotateCW(2,other_axis);
        }

        for (Direction.Axis axis : Direction.Axis.values()){
            if (isRotatedCW(axis,present_direction,changing_direction)){
                return this.rotateCW(1, axis);
            }

            if (isRotatedCCW(axis,present_direction,changing_direction)){
                return this.rotateCCW(1,axis);
            }

        }
        return null;

    }

    private boolean isRotatedCCW(Direction.Axis axis, Direction present_direction, Direction changing_direction) {
        return isRotatedCW(axis,changing_direction,present_direction);
    }


    private boolean isRotatedCW(Direction.Axis axis,Direction present_direction,Direction changing_direction) {
        int pd_ind = present_direction.get3DDataValue();
        int cd_ind = changing_direction.get3DDataValue();
        switch (axis){
            case X:
                return (pd_ind==0 && cd_ind==2) ||
                        (pd_ind==1 && cd_ind==3) ||
                        (pd_ind==2 && cd_ind==1) ||
                        (pd_ind==3 && cd_ind==0);
            case Y:
                return (pd_ind==4 && cd_ind==2) ||
                        (pd_ind==5 && cd_ind==3) ||
                        (pd_ind==2 && cd_ind==5) ||
                        (pd_ind==3 && cd_ind==4);
            case Z:
                return (pd_ind==0 && cd_ind==4) ||
                        (pd_ind==1 && cd_ind==5) ||
                        (pd_ind==4 && cd_ind==1) ||
                        (pd_ind==5 && cd_ind==0);
        }
        return false;
    }


    //make the symmetry around the plane formed by the two axis given if distinct
    public VoxelDoubles makeSymmetry(Direction.Axis axis1, Direction.Axis axis2){
        if (axis1 == axis2){
            throw new IllegalArgumentException("The Axis selected are identical no axial symmetry can be done");
        }
        double x1 = planeVal[0];
        double y1 = planeVal[1];
        double z1 = planeVal[2];
        double x2 = planeVal[3];
        double y2 = planeVal[4];
        double z2 = planeVal[5];
        //symmetry regarding XY plane
        if ((axis1== Direction.Axis.X && axis2== Direction.Axis.Y)
                ||(axis1== Direction.Axis.Y && axis2== Direction.Axis.X)){
            return new VoxelDoubles(x1,y1,16-z2,x2,y2,16-z1,false);
        }
        //symmetry regarding YZ plane
        if ((axis1== Direction.Axis.Z && axis2== Direction.Axis.Y)
                ||(axis1== Direction.Axis.Y && axis2== Direction.Axis.Z)){
            return new VoxelDoubles(16-x2,y1,z1,16-x1,y2,z2,false);
        }
        //symmetry regarding XZ plane
        if ((axis1== Direction.Axis.X && axis2== Direction.Axis.Z)
                ||(axis1== Direction.Axis.Z && axis2== Direction.Axis.X)){
            return new VoxelDoubles(x1,16-y2,z1,x2,16-y1,z2,false);
        }
        return null;
    }

    public VoxelShape getAssociatedShape(){
        if (this == EMPTY){
            return Shapes.empty();
        }else if (this == FULL){
            return Shapes.block();
        }
        if (planeVal[0]> planeVal[3]){
            double pv0 = planeVal[0];
            planeVal[0] = planeVal[3];
            planeVal[3] = pv0;
        }
        if (planeVal[1]> planeVal[4]){
            double pv1 = planeVal[1];
            planeVal[1] = planeVal[4];
            planeVal[4] = pv1;
        }
        if (planeVal[2]> planeVal[5]){
            double pv2 = planeVal[2];
            planeVal[2] = planeVal[5];
            planeVal[5] = pv2;
        }
        return Block.box(
                planeVal[0], planeVal[1], planeVal[2],
                planeVal[3], planeVal[4], planeVal[5]);
    }
}
