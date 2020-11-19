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
    public static Direction getDirectionFromEntity(LivingEntity placer,BlockPos pos){
        Vec3d vec3d = placer.getPositionVec();
        Direction d= Direction.getFacingFromVector(vec3d.x-pos.getX(),vec3d.y-pos.getY(),vec3d.z-pos.getZ());
        if (d== Direction.DOWN || d== Direction.UP){
            return Direction.NORTH;
        }
        return d;
    }

    public static ExtendDirection getDirectionFromEntityAndNeighbor(LivingEntity placer, BlockPos pos, World world){
        //todo : change getDirectionFromEntity to match with rotated windows
        ExtendDirection defaultDir = ExtendDirection.getFacingFromPlayer(placer,pos);
        Vec3d vec3d = placer.getPositionVec();
        boolean ns = false;
        boolean ew = false;
        boolean nwse = false;
        boolean nesw = false;
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
        for (int i=0;i<4;i++){
            // fd = [SOUTH WEST NORTH EAST]
            Direction first_dir = Direction.byHorizontalIndex(i);
            // sd = [WEST NORTH EAST SOUTH]
            Direction second_dir = Direction.byHorizontalIndex((i+1)%4);
            BlockPos neighPos = pos.offset(first_dir).offset(second_dir);
            BlockState neighState = world.getBlockState(neighPos);
            if (neighState.getMaterial().blocksMovement()){
                //SW or NE
                if (i==0 || i==2){
                    nesw = true;
                }else{ //NW or SE
                    nwse = true;
                }
            }
        }
        //if ns or ew are impossible
        if (ns == ew){
            //overfilling of windows surrounding (incompatible places)
            if (ns || nesw == nwse){
                return defaultDir;
            }else if (nesw){
                boolean playerIsInNorthEast = vec3d.x+vec3d.z < pos.getX()+pos.getZ()+1.0F;
                return (playerIsInNorthEast)? ExtendDirection.NORTH_WEST : ExtendDirection.SOUTH_EAST;
            }else {
                boolean playerIsInNorthWest = vec3d.x-vec3d.z > pos.getX()-pos.getZ();
                return (playerIsInNorthWest)? ExtendDirection.NORTH_EAST: ExtendDirection.SOUTH_WEST;
            }
        }else if (neiWindowFacing != null) {
            //if we have a near windows (with ew or ns)
            return ExtendDirection.getExtendedDirection(neiWindowFacing,false);
        }else if (ns){
            boolean playerIsInWest = vec3d.x > pos.getX()+0.5F;
            return (playerIsInWest)? ExtendDirection.EAST : ExtendDirection.WEST;
        }else{
            boolean playerIsInSouth = vec3d.z > pos.getZ()+0.5F;
            return (playerIsInSouth)? ExtendDirection.SOUTH : ExtendDirection.NORTH;
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
