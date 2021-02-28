package fr.mattmouss.gates.util;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.voxels.VoxelInts;
import fr.mattmouss.gates.windows.WindowBlock;
import fr.mattmouss.gates.windows.WindowPlace;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import java.util.List;

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
        WindowBlock window = (WindowBlock)world.getBlockState(pos).getBlock();
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
            if (window.equals(neighState.getBlock())){
                //check for problem in neighbor block that are rotated (future non rotated block here)
                if (neighState.get(WindowBlock.WINDOW_PLACE) == WindowPlace.FULL || !neighState.get(WindowBlock.ROTATED)) {
                    //we need to avoid problems with windows with bad facing
                    if (neighState.get(BlockStateProperties.HORIZONTAL_FACING).getAxis() != dir.getAxis()) {
                        neiWindowFacing = neighState.get(BlockStateProperties.HORIZONTAL_FACING);
                        if (i == 2 || i == 3) {
                            ns = true;
                        } else if (i == 4 || i == 5) {
                            ew = true;
                        }
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

    public static VoxelShape getShapeFromVoxelIntsTab(VoxelInts[] usedVoxels,Direction facing,VoxelShape shape){
        for (VoxelInts vi : usedVoxels){
            vi = vi.rotate(Direction.EAST,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }
        return shape;
    }

    public static boolean testReplaceable(BlockItemUseContext context,BlockPos... positions){
        boolean isReplaceable = true;
        for (BlockPos pos : positions){
            isReplaceable = isReplaceable && context.getWorld().getBlockState(pos).isReplaceable(context);
        }
        return isReplaceable;
    }

    public static VoxelShape makeCircleShape(DoorPlacing placing,Direction facing,boolean isOpen) {
        List<VoxelInts> voxels = Lists.newArrayList();
        //voxels for right place
        if (!isOpen){
            //a unique voxels is added
            voxels.add(new VoxelInts(0,0,0,3,16,16,true));
            if (placing == DoorPlacing.RIGHT_DOWN || placing == DoorPlacing.LEFT_DOWN) {
                //handle on 2 sides
                //handle on out side
                //horizontal part
                voxels.add(new VoxelInts(-1, 15, 12, 1, 1, 2, true));
                voxels.add(new VoxelInts(-1, 12, 12, 1, 1, 2, true));
                //vertical part
                voxels.add(new VoxelInts(-1, 13, 11, 1, 2, 1, true));
                voxels.add(new VoxelInts(-1, 13, 14, 1, 2, 1, true));
                //handle on in side
                //horizontal part
                voxels.add(new VoxelInts(3, 15, 12, 1, 1, 2, true));
                voxels.add(new VoxelInts(3, 12, 12, 1, 1, 2, true));
                //vertical part
                voxels.add(new VoxelInts(3, 13, 11, 1, 2, 1, true));
                voxels.add(new VoxelInts(3, 13, 14, 1, 2, 1, true));
            }
        }else {
            //not moving part for all right part
            voxels.add(new VoxelInts(0, 0, 0, 3, 16, 4, true));
            if (placing == DoorPlacing.RIGHT_DOWN || placing == DoorPlacing.LEFT_DOWN) {
                //handle on 2 sides
                //handle on out side
                //horizontal part
                voxels.add(new VoxelInts(11, 15, 7, 2, 1, 1, true));
                voxels.add(new VoxelInts(11, 12, 7, 2, 1, 1, true));
                //vertical part
                voxels.add(new VoxelInts(10, 13, 7, 1, 2, 1, true));
                voxels.add(new VoxelInts(13, 13, 7, 1, 2, 1, true));
                //handle on in side
                //horizontal part
                voxels.add(new VoxelInts(11, 15, 3, 2, 1, 1, true));
                voxels.add(new VoxelInts(11, 12, 3, 2, 1, 1, true));
                //vertical part
                voxels.add(new VoxelInts(10, 13, 3, 1, 2, 1, true));
                voxels.add(new VoxelInts(13, 13, 3, 1, 2, 1, true));
            }
            if (placing.isUp()) {
                //when up right position
                //above door shape
                voxels.add(new VoxelInts(0, 11, 4, 3, 5, 11, true));
                //circle not moving shape
                voxels.add(new VoxelInts(0, 10, 4, 3, 1, 11, true));
                voxels.add(new VoxelInts(0, 9, 4, 3, 1, 8, true));
                voxels.add(new VoxelInts(0, 8, 4, 3, 1, 6, true));
                voxels.add(new VoxelInts(0, 7, 4, 3, 1, 5, true));
                voxels.add(new VoxelInts(0, 6, 4, 3, 1, 4, true));
                voxels.add(new VoxelInts(0, 5, 4, 3, 1, 3, true));
                voxels.add(new VoxelInts(0, 3, 4, 3, 2, 2, true));
                voxels.add(new VoxelInts(0, 0, 4, 3, 3, 1, true));
                //open door part
                voxels.add(new VoxelInts(14, 10, 4, 1, 1, 3, true));
                voxels.add(new VoxelInts(11, 9, 4, 4, 1, 3, true));
                voxels.add(new VoxelInts(9, 8, 4, 6, 1, 3, true));
                voxels.add(new VoxelInts(8, 7, 4, 7, 1, 3, true));
                voxels.add(new VoxelInts(7, 6, 4, 8, 1, 3, true));
                voxels.add(new VoxelInts(6, 5, 4, 9, 1, 3, true));
                voxels.add(new VoxelInts(5, 3, 4, 10, 2, 3, true));
                voxels.add(new VoxelInts(4, 0, 4, 11, 3, 3, true));
            } else {
                //moving part
                voxels.add(new VoxelInts(3, 0, 4, 12, 16, 3, true));
            }
        }

        if (placing.isLeft()){
            int len = voxels.size();
            for (int i=0;i<len;i++){
                VoxelInts oldVoxel = voxels.get(i);
                VoxelInts symVoxel = oldVoxel.makeSymetry(Direction.Axis.X, Direction.Axis.Y);
                voxels.set(i,symVoxel);
            }
        }
        VoxelShape shape = VoxelShapes.empty();
        for (VoxelInts vi : voxels){
            shape = VoxelShapes.or(shape, vi.rotate(Direction.EAST, facing).getAssociatedShape());
        }
        return shape;
    }

    public static VoxelShape makeSquareShape(DoorPlacing placing,Direction facing,boolean isOpen){
        if (!isOpen || placing.isDown() || placing.isCenterY()){
            return makeCircleShape(placing,facing, isOpen);
        }
        List<VoxelInts> voxels = Lists.newArrayList();
        //door fixed part
        voxels.add(new VoxelInts(0,0, 0,3,16,4,true));
        voxels.add(new VoxelInts(0,11,4,3,5,12,true));
        //door part
        voxels.add(new VoxelInts(3,0,4,12,11,3,true));
        if (placing.isLeft()){
            for (int i=0;i<3;i++){
                VoxelInts oldVoxel = voxels.get(i);
                VoxelInts symVoxel = oldVoxel.makeSymetry(Direction.Axis.X, Direction.Axis.Y);
                voxels.set(i,symVoxel);
            }
        }
        VoxelShape shape = VoxelShapes.empty();
        for (VoxelInts vi : voxels){
            shape = VoxelShapes.or(shape, vi.rotate(Direction.EAST, facing).getAssociatedShape());
        }
        return shape;
    }


}
