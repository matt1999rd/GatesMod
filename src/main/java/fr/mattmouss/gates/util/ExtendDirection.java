package fr.mattmouss.gates.util;

import com.mojang.math.Vector3d;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;


public enum ExtendDirection {
    SOUTH(0, Direction.SOUTH,Axis.Z,Direction.AxisDirection.POSITIVE,false),
    WEST(1, Direction.WEST,Axis.X,Direction.AxisDirection.NEGATIVE,false),
    NORTH(2, Direction.NORTH,Axis.Z,Direction.AxisDirection.NEGATIVE,false),
    EAST(3, Direction.EAST,Axis.X,Direction.AxisDirection.POSITIVE,false),
    SOUTH_WEST(4, Direction.WEST,Axis.XPZ,Direction.AxisDirection.NEGATIVE,true),
    NORTH_WEST(5, Direction.NORTH,Axis.XMZ,Direction.AxisDirection.NEGATIVE,true),
    NORTH_EAST(6, Direction.EAST,Axis.XPZ,Direction.AxisDirection.POSITIVE,true),
    SOUTH_EAST(7, Direction.SOUTH,Axis.XMZ,Direction.AxisDirection.POSITIVE,true),
    UP(8,Direction.UP,Axis.Y,Direction.AxisDirection.POSITIVE,false),
    DOWN(9,Direction.DOWN,Axis.Y,Direction.AxisDirection.NEGATIVE,false);

    private final int meta;
    private final Direction direction;
    private final Axis axis;
    private final Direction.AxisDirection axisDirection;
    private final boolean rotated;

    ExtendDirection(int meta, Direction direction,Axis axis,Direction.AxisDirection axisDirection, boolean rotated){
        this.meta = meta;
        this.direction = direction;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.rotated = rotated;
    }

    public int getIndex(){
        return meta;
    }

    public static ExtendDirection getExtendedDirection(Direction dir, boolean isRotated){
        switch (dir){
            case DOWN:
            case UP:
            default:
                return null;
            case NORTH:
                return (isRotated)?ExtendDirection.NORTH_WEST:ExtendDirection.NORTH;
            case SOUTH:
                return (isRotated)?ExtendDirection.SOUTH_EAST:ExtendDirection.SOUTH;
            case WEST:
                return (isRotated)?ExtendDirection.SOUTH_WEST:ExtendDirection.WEST;
            case EAST:
                return (isRotated)?ExtendDirection.NORTH_EAST:ExtendDirection.EAST;
        }
    }

    public ExtendDirection getOpposite(){
        return getExtendedDirection(this.direction.getOpposite(),this.rotated);
    }

    public BlockPos offset(BlockPos pos){
        if (!this.isRotated()){
            return pos.relative(this.direction);
        }else {
            return pos.relative(this.direction).relative(this.direction.getCounterClockWise());
        }
    }

    public BlockPos offset(BlockPos pos,int n){
        if (!this.isRotated()){
            return pos.relative(this.direction,n);
        } else {
            return pos.relative(this.direction,n).relative(this.direction.getCounterClockWise(),n);
        }
    }

    public ExtendDirection rotateYCCW(){
        return ExtendDirection.getExtendedDirection(direction.getCounterClockWise(),isRotated());
    }

    public ExtendDirection rotateY(){
        return ExtendDirection.getExtendedDirection(direction.getClockWise(),isRotated());
    }

    public static ExtendDirection byIndex(int meta){
        ExtendDirection[] directions = ExtendDirection.values();
        if (meta<0 || meta>9)return null;
        return directions[meta];
    }


    public int getMeta() {
        return meta;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isRotated() {
        return rotated;
    }

    public Direction.AxisDirection getAxisDirection() {
        return axisDirection;
    }

    public static double toDegree(double radianAngle){
        double degreeAngle = (180.0/Math.PI)*radianAngle;
        if (degreeAngle < 0){
            degreeAngle +=360.0D;
        }
        return degreeAngle;
    }

    public Axis getAxis(){
        return axis;
    }

    public static ExtendDirection getFacingFromPlayer(LivingEntity player, BlockPos pos){
        //for the purpose of getting the state of the panel we divide space around the center of the support in 8 part
        //division are for angle 22.5/67.5/112.5/157.5/202.5/247.5/292.5/337.5 (22.5+45*i for 0<=i<=7)
        Vec3 future_window_center = getVecFromBlockPos(pos,0.5F);
        Vec3 offsetPlayerPos = player.position().subtract(future_window_center);
        //we compare our player position to the position of the support's center
        //we get angle using arc-tan function
        double angle = Mth.atan2(offsetPlayerPos.x,offsetPlayerPos.z);
        //we convert to degree and make it positive
        double degreeAngle =toDegree(angle);
        //then to index from 2 to 9 corresponding to the part of stage where the player is
        int index = Mth.ceil((degreeAngle-22.5D)/45.0D);
        //we consider the part split by angle origin which correspond to 0
        // that we move of a complete circle for math simplification
        if (index == 0){
            index =8;
        }else if (index == 1){
            index =9;
        }
        //we get facing using a special index that is for i : 0->7 --> 0 0 3 3 2 2 1 1
        //we have translated 0 to 8 and 1 to 9 to get a decreasing linear function -->
        // 2->3 3->3 4->2 5->2 6->1 7->1 8->0 9->0
        Direction facing = Direction.from2DDataValue((9-index)/2);
        boolean isRotated = (index%2 == 1);
        return ExtendDirection.getExtendedDirection(facing,isRotated);
    }

    //convert a block position into vec3d and adding an offset on x and z coordinate
    // (use in the conversion to vec3d of support and grid center block position)

    public static Vec3 getVecFromBlockPos (BlockPos pos,float horOffset){
        return new Vec3(pos.getX()+horOffset,pos.getY(),pos.getZ()+horOffset);
    }

    public enum Axis{
        X,
        Y,
        Z,
        XPZ,
        XMZ;

        public boolean isOnPlane(Axis frontalAxis) {
            if (this == Y)return true;
            switch (frontalAxis){
                case X:
                    return (this == Z);
                case Z:
                    return (this == X);
                case XMZ:
                    return (this == XPZ);
                case XPZ:
                    return (this == XMZ);
                case Y:
                    return true;
            }
            return false;
        }

        public double project(Vec3 vec3){
            switch (this) {
                case X:
                    return vec3.x;
                case Y:
                    return vec3.y;
                case Z:
                    return vec3.z;
                case XMZ:
                    return (vec3.x + vec3.z)/Math.sqrt(2);
                case XPZ:
                    return (vec3.x - vec3.z)/Math.sqrt(2);
            }
            return 0; //unreachable code
        }
    }



}
