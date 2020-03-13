package fr.mattmouss.gates.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Functions {
    public static Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos) {
        Vec3d vec = placer.getPositionVec();
        Direction d = Direction.getFacingFromVector(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (d==Direction.DOWN || d==Direction.UP){
            return Direction.NORTH;
        }
        return d;
    }

    public static Direction getDirectionOfExtBlock(Direction direction, DoorHingeSide dhs){
        return  (dhs == DoorHingeSide.RIGHT)?direction.rotateYCCW():direction.rotateY();
    }

    public static DoorHingeSide getHingeSideFromEntity(LivingEntity entity, BlockPos pos, Direction direction) {
        switch (direction){
            case DOWN:
            case UP:
            default:
                throw new IllegalArgumentException("No such direction authorised !!");
            case NORTH:
                return (entity.getPositionVec().x<pos.getX()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
            case SOUTH:
                return (entity.getPositionVec().x<pos.getX()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case WEST:
                return (entity.getPositionVec().z<pos.getZ()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case EAST:
                return (entity.getPositionVec().z<pos.getZ()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
        }
    }

    public static int getIdFromBlockPos(BlockPos pos){
        int nz= pos.getX();
        int m = pos.getY();
        int pz= pos.getZ();
        int n = BijectZN(nz);
        int p = BijectZN(pz);
        //development of function f(f(n,m),p) where f(n,m) is a bijection from NxN to N
        int id = (((n+m)^2+n+3*m+2*p)^2+2*(n+m)^2+2*n+6*m+12*p)/8;
        return id;
    }

    private static int BijectZN(int z){
        if (z<=0){
            return -2*z;
        }else {
            return 2*z+1;
        }
    }



    public static double Distance3(double[] first_pos, double[] player_pos) {
        return MathHelper.sqrt(
                Math.pow(first_pos[0]-player_pos[0],2.0)+
                        Math.pow(first_pos[1]-player_pos[1],2.0)+
                        Math.pow(first_pos[2]-player_pos[2],2.0)) ;

    }
}
