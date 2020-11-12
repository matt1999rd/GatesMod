package fr.mattmouss.gates.util;

import fr.mattmouss.gates.windows.WindowBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class Functions {
    public static Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos) {
        Vec3d vec = placer.getPositionVec();
        Direction d = Direction.getFacingFromVector(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (d==Direction.DOWN || d==Direction.UP){
            return Direction.NORTH;
        }
        return d;
    }

    public static Direction getDirectionFromEntityAndNeighbor(LivingEntity placer, BlockPos pos, World world){
        Direction defaultDir = getDirectionFromEntity(placer,pos);
        Vec3d vec3d = placer.getPositionVec();
        boolean ns = false;
        boolean ew = false;
        Direction neiWindowFacing = null;
        for (int i=0;i<6;i++){
            Direction dir = Direction.byIndex(i);
            BlockPos neighPos = pos.offset(dir);
            BlockState neighState = world.getBlockState(neighPos);
            if (neighState.getBlock() instanceof WindowBlock){
                //we need to avoid problems with windows with bad facing
                if (neighState.get(BlockStateProperties.HORIZONTAL_FACING).getAxis() != dir.getAxis()) {
                    neiWindowFacing = neighState.get(BlockStateProperties.HORIZONTAL_FACING);
                    if (i==2 || i==3) {
                        ns = true;
                    } else if (i==4 || i==5){
                        ew = true;
                    }
                }
            }else if (neighState.getMaterial().blocksMovement()) {
                if (i==2 || i==3){
                    ns = true;
                }else if (i==4 || i==5){
                    ew = true;
                }
            }
        }
        if (ns == ew){
            return defaultDir;
        }else if (neiWindowFacing != null) {
            return neiWindowFacing;
        }else if (ns){
            boolean playerIsInWest = vec3d.x > pos.getX()+0.5F;
            return (playerIsInWest)? Direction.EAST : Direction.WEST;
        }else{
            boolean playerIsInSouth = vec3d.z > pos.getZ()+0.5F;
            return (playerIsInSouth)? Direction.SOUTH : Direction.NORTH;
        }
    }

    //give the side where the barrier is extending
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
