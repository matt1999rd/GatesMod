package fr.mattmouss.gates.util;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.enum_door.DrawBridgePosition;
import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.voxels.VoxelDoubles;
import fr.mattmouss.gates.windows.WindowBlock;
import fr.mattmouss.gates.windows.WindowPlace;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class Functions {

    public static void moveMainOldStackToFreeSlot(PlayerEntity entity){
        ItemStack oldStack = entity.getItemBySlot(EquipmentSlotType.MAINHAND);
        oldStack.shrink(1);
        int freeSlot= entity.inventory.getFreeSlot();
        if (freeSlot != -1){
            entity.setSlot(freeSlot,oldStack);
        }else {
            entity.spawnAtLocation(oldStack);
        }
    }
    public static Direction getDirectionFromEntity(LivingEntity placer,BlockPos pos){
        Vector3d vec3d = placer.position();
        Direction d= Direction.getNearest(vec3d.x-pos.getX(),vec3d.y-pos.getY(),vec3d.z-pos.getZ());
        if (d== Direction.DOWN || d== Direction.UP){
            return Direction.NORTH;
        }
        return d;
    }

    public static ExtendDirection getDirectionFromEntityAndNeighbor(LivingEntity placer, BlockPos pos, World world){
        WindowBlock window = (WindowBlock)world.getBlockState(pos).getBlock();
        ExtendDirection defaultDir = ExtendDirection.getFacingFromPlayer(placer,pos);
        Vector3d vec3d = placer.position();
        boolean northSouth = false;
        boolean eastWest = false;
        boolean northWestSouthEast = false;
        boolean northEastSouthWest = false;
        Direction neiWindowFacing = null;
        for (int i=0;i<6;i++){
            Direction dir = Direction.from3DDataValue(i);
            BlockPos neighPos = pos.relative(dir);
            BlockState neighState = world.getBlockState(neighPos);
            if (window.equals(neighState.getBlock())){
                //check for problem in neighbor block that are rotated (future non-rotated block here)
                if (neighState.getValue(WindowBlock.WINDOW_PLACE) == WindowPlace.FULL || !neighState.getValue(WindowBlock.ROTATED)) {
                    //we need to avoid problems with windows with bad facing
                    if (neighState.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() != dir.getAxis()) {
                        neiWindowFacing = neighState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        if (i == 2 || i == 3) {
                            northSouth = true;
                        } else if (i == 4 || i == 5) {
                            eastWest = true;
                        }
                    }
                }
            }else if (neighState.getMaterial().blocksMotion()) {
                if (i==2 || i==3){
                    northSouth = true;
                }else if (i==4 || i==5){
                    eastWest = true;
                }
            }
        }
        for (int i=0;i<4;i++){
            // fd = [SOUTH WEST NORTH EAST]
            Direction first_dir = Direction.from2DDataValue(i);
            // sd = [WEST NORTH EAST SOUTH]
            Direction second_dir = Direction.from2DDataValue((i+1)%4);
            BlockPos neighPos = pos.relative(first_dir).relative(second_dir);
            BlockState neighState = world.getBlockState(neighPos);
            if (neighState.getMaterial().blocksMotion()){
                //SW or NE
                if (i==0 || i==2){
                    northEastSouthWest = true;
                }else{ //NW or SE
                    northWestSouthEast = true;
                }
            }
        }
        //if ns or ew are impossible
        if (northSouth == eastWest){
            //overfilling of windows surrounding (incompatible places)
            if (northSouth || northEastSouthWest == northWestSouthEast){
                return defaultDir;
            }else if (northEastSouthWest){
                boolean playerIsInNorthEast = vec3d.x+vec3d.z < pos.getX()+pos.getZ()+1.0F;
                return (playerIsInNorthEast)? ExtendDirection.NORTH_WEST : ExtendDirection.SOUTH_EAST;
            }else {
                boolean playerIsInNorthWest = vec3d.x-vec3d.z > pos.getX()-pos.getZ();
                return (playerIsInNorthWest)? ExtendDirection.NORTH_EAST: ExtendDirection.SOUTH_WEST;
            }
        }else if (neiWindowFacing != null) {
            //if we have a near windows (with ew or ns)
            return ExtendDirection.getExtendedDirection(neiWindowFacing,false);
        }else if (northSouth){
            boolean playerIsInWest = vec3d.x > pos.getX()+0.5F;
            return (playerIsInWest)? ExtendDirection.EAST : ExtendDirection.WEST;
        }else{
            boolean playerIsInSouth = vec3d.z > pos.getZ()+0.5F;
            return (playerIsInSouth)? ExtendDirection.SOUTH : ExtendDirection.NORTH;
        }
    }

    //give the side where the barrier is extending
    public static Direction getDirectionOfExtBlock(Direction direction, DoorHingeSide dhs){
        return  (dhs == DoorHingeSide.RIGHT)?direction.getCounterClockWise():direction.getClockWise();
    }

    public static DoorHingeSide getHingeSideFromEntity(LivingEntity entity, BlockPos pos, Direction direction) {
        switch (direction){
            case DOWN:
            case UP:
            default:
                throw new IllegalArgumentException("No such direction authorised !!");
            case NORTH:
                return (entity.position().x<pos.getX()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
            case SOUTH:
                return (entity.position().x<pos.getX()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case WEST:
                return (entity.position().z<pos.getZ()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case EAST:
                return (entity.position().z<pos.getZ()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
        }
    }

    public static BlockPos getMainPosition(BlockPos pos, LivingEntity entity){
        Direction direction = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,direction);
        return (dhs == DoorHingeSide.RIGHT) ? pos.relative(direction.getClockWise()): pos.relative(direction.getCounterClockWise());
    }

    public static TurnSPosition getCUPosition(BlockPos pos,LivingEntity player){
        Direction direction = Functions.getDirectionFromEntity(player,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(player,pos,direction);
        //boolean that return true when Control Unit is on the right
        boolean CUisOnRight = (dhs == DoorHingeSide.RIGHT);
        //the control unit block (left if DHS.left and right if DHS.right)
        return (CUisOnRight) ? TurnSPosition.RIGHT_BLOCK : TurnSPosition.LEFT_BLOCK;
    }

    public static int getIdFromBlockPos(BlockPos pos){
        int nz= pos.getX();
        int m = pos.getY();
        int pz= pos.getZ();
        int n = BijectionZN(nz);
        int p = BijectionZN(pz);
        //development of function f(f(n,m),p) where f(n,m) is a bijection from NxN to N
        return (((n+m)^2+n+3*m+2*p)^2+2*(n+m)^2+2*n+6*m+12*p)/8;
    }

    private static int BijectionZN(int z){
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

    public static VoxelShape getShapeFromVoxelIntsTab(VoxelDoubles[] usedVoxels, Direction facing, VoxelShape shape){
        for (VoxelDoubles vi : usedVoxels){
            vi = vi.rotate(Direction.EAST,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }
        return shape;
    }

    public static VoxelShape getShapeFromVoxelIntsTab(List<VoxelDoubles> usedVoxels,Direction facing,VoxelShape shape){
        for (VoxelDoubles vi : usedVoxels){
            vi = vi.rotate(Direction.EAST,facing);
            shape = VoxelShapes.or(shape,vi.getAssociatedShape());
        }
        return shape;
    }

    public static boolean testReplaceable(BlockItemUseContext context,BlockPos... positions){
        boolean isReplaceable = true;
        for (BlockPos pos : positions){
            isReplaceable = isReplaceable && context.getLevel().getBlockState(pos).canBeReplaced(context);
        }
        return isReplaceable;
    }



    public static VoxelShape makeCircleShape(DoorPlacing placing,Direction facing,boolean isOpen) {
        List<VoxelDoubles> voxels = Lists.newArrayList();
        //voxels for right place
        if (!isOpen){
            //a unique voxels is added
            voxels.add(new VoxelDoubles(0,0,0,3,16,16,true));
            if (placing == DoorPlacing.RIGHT_DOWN || placing == DoorPlacing.LEFT_DOWN) {
                //handle on 2 sides
                //handle on outside part
                //horizontal part
                voxels.add(new VoxelDoubles(-1, 15, 12, 1, 1, 2, true));
                voxels.add(new VoxelDoubles(-1, 12, 12, 1, 1, 2, true));
                //vertical part
                voxels.add(new VoxelDoubles(-1, 13, 11, 1, 2, 1, true));
                voxels.add(new VoxelDoubles(-1, 13, 14, 1, 2, 1, true));
                //handle on inside part
                //horizontal part
                voxels.add(new VoxelDoubles(3, 15, 12, 1, 1, 2, true));
                voxels.add(new VoxelDoubles(3, 12, 12, 1, 1, 2, true));
                //vertical part
                voxels.add(new VoxelDoubles(3, 13, 11, 1, 2, 1, true));
                voxels.add(new VoxelDoubles(3, 13, 14, 1, 2, 1, true));
            }
        }else {
            //not moving part for all right part
            voxels.add(new VoxelDoubles(0, 0, 0, 3, 16, 4, true));
            if (placing == DoorPlacing.RIGHT_DOWN || placing == DoorPlacing.LEFT_DOWN) {
                //handle on 2 sides
                //handle on outside part
                //horizontal part
                voxels.add(new VoxelDoubles(11, 15, 7, 2, 1, 1, true));
                voxels.add(new VoxelDoubles(11, 12, 7, 2, 1, 1, true));
                //vertical part
                voxels.add(new VoxelDoubles(10, 13, 7, 1, 2, 1, true));
                voxels.add(new VoxelDoubles(13, 13, 7, 1, 2, 1, true));
                //handle on inside part
                //horizontal part
                voxels.add(new VoxelDoubles(11, 15, 3, 2, 1, 1, true));
                voxels.add(new VoxelDoubles(11, 12, 3, 2, 1, 1, true));
                //vertical part
                voxels.add(new VoxelDoubles(10, 13, 3, 1, 2, 1, true));
                voxels.add(new VoxelDoubles(13, 13, 3, 1, 2, 1, true));
            }
            if (placing.isUp()) {
                //when up right position
                //above door shape
                voxels.add(new VoxelDoubles(0, 11, 4, 3, 5, 11, true));
                //circle not moving shape
                voxels.add(new VoxelDoubles(0, 10, 4, 3, 1, 11, true));
                voxels.add(new VoxelDoubles(0, 9, 4, 3, 1, 8, true));
                voxels.add(new VoxelDoubles(0, 8, 4, 3, 1, 6, true));
                voxels.add(new VoxelDoubles(0, 7, 4, 3, 1, 5, true));
                voxels.add(new VoxelDoubles(0, 6, 4, 3, 1, 4, true));
                voxels.add(new VoxelDoubles(0, 5, 4, 3, 1, 3, true));
                voxels.add(new VoxelDoubles(0, 3, 4, 3, 2, 2, true));
                voxels.add(new VoxelDoubles(0, 0, 4, 3, 3, 1, true));
                //open door part
                voxels.add(new VoxelDoubles(14, 10, 4, 1, 1, 3, true));
                voxels.add(new VoxelDoubles(11, 9, 4, 4, 1, 3, true));
                voxels.add(new VoxelDoubles(9, 8, 4, 6, 1, 3, true));
                voxels.add(new VoxelDoubles(8, 7, 4, 7, 1, 3, true));
                voxels.add(new VoxelDoubles(7, 6, 4, 8, 1, 3, true));
                voxels.add(new VoxelDoubles(6, 5, 4, 9, 1, 3, true));
                voxels.add(new VoxelDoubles(5, 3, 4, 10, 2, 3, true));
                voxels.add(new VoxelDoubles(4, 0, 4, 11, 3, 3, true));
            } else {
                //moving part
                voxels.add(new VoxelDoubles(3, 0, 4, 12, 16, 3, true));
            }
        }

        if (placing.isLeft()){
            int len = voxels.size();
            for (int i=0;i<len;i++){
                VoxelDoubles oldVoxel = voxels.get(i);
                VoxelDoubles symVoxel = oldVoxel.makeSymmetry(Direction.Axis.X, Direction.Axis.Y);
                voxels.set(i,symVoxel);
            }
        }
        return getShapeFromVoxelIntsTab(voxels,facing,VoxelShapes.empty());
    }

    public static VoxelShape makeSquareShape(DoorPlacing placing,Direction facing,boolean isOpen){
        if (!isOpen || placing.isDown() || placing.isCenterY()){
            return makeCircleShape(placing,facing, isOpen);
        }
        List<VoxelDoubles> voxels = Lists.newArrayList();
        //door fixed part
        voxels.add(new VoxelDoubles(0,0, 0,3,16,4,true));
        voxels.add(new VoxelDoubles(0,11,4,3,5,12,true));
        //door part
        voxels.add(new VoxelDoubles(3,0,4,12,11,3,true));
        if (placing.isLeft()){
            for (int i=0;i<3;i++){
                VoxelDoubles oldVoxel = voxels.get(i);
                VoxelDoubles symVoxel = oldVoxel.makeSymmetry(Direction.Axis.X, Direction.Axis.Y);
                voxels.set(i,symVoxel);
            }
        }
        return getShapeFromVoxelIntsTab(voxels,facing,VoxelShapes.empty());
    }

    public static VoxelShape makeGardenDoorShape(DoorPlacing placing, Direction facing, boolean isOpen){
        List<VoxelDoubles> voxels = Lists.newArrayList();
        //support
        voxels.add(new VoxelDoubles(0,0,12,4,16,4,true));
        if (isOpen){
            if (placing.isDown()) {
                //vertical grid
                voxels.add(new VoxelDoubles(6, 8, 13, 2, 8, 2, true));
                voxels.add(new VoxelDoubles(10, 8, 13, 2, 8, 2, true));
                voxels.add(new VoxelDoubles(14, 8, 13, 2, 8, 2, true));
                // bottom of the door
                // base
                voxels.add(new VoxelDoubles(4, 0, 13, 12, 8, 2, true));
                // rectangle decoration for visible part
                voxels.add(new VoxelDoubles(14, 1, 12, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(6,  1, 12, 8, 1, 1, true));
                voxels.add(new VoxelDoubles(5,  1, 12, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(6,  6, 12, 8, 1, 1, true));
                // square center for visible part
                voxels.add(new VoxelDoubles(9,  3, 12, 2, 2, 1, true));
                // rectangle decoration for hidden part
                voxels.add(new VoxelDoubles(5,  1, 15, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(6,  6, 15, 8, 1, 1, true));
                voxels.add(new VoxelDoubles(14, 1, 15, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(6,  1, 15, 8, 1, 1, true));
                // square center for hidden part
                voxels.add(new VoxelDoubles(9,  3, 15, 2, 2, 1, true));
            } else {
                //vertical grid
                voxels.add(new VoxelDoubles(6,  0, 13, 2,  7, 2, true));
                voxels.add(new VoxelDoubles(10, 0, 13, 2, 11, 2, true));
                voxels.add(new VoxelDoubles(14, 0, 13, 2, 16, 2, true));
                //diagonal grid
                for (int i = 4; i < 14; i++) {
                    voxels.add(new VoxelDoubles(i, i - 1, 13, 1, 2, 2, true));
                }
            }
        }else {
            if (placing.isDown()) {
                //vertical grid
                voxels.add(new VoxelDoubles(1, 8, 8, 2, 8, 2, true));
                voxels.add(new VoxelDoubles(1, 8, 4, 2, 8, 2, true));
                voxels.add(new VoxelDoubles(1, 8, 0, 2, 8, 2, true));
                // bottom of the door
                // base
                voxels.add(new VoxelDoubles(1, 0, 0, 2, 8, 12, true));
                // rectangle decoration for visible part
                voxels.add(new VoxelDoubles(3, 1, 10, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(3, 6,  2, 1, 1, 8, true));
                voxels.add(new VoxelDoubles(3, 1,  1, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(3, 1,  2, 1, 1, 8, true));
                // square center for visible part
                voxels.add(new VoxelDoubles(3, 3,  5, 1, 2, 2, true));
                // rectangle decoration for hidden part
                voxels.add(new VoxelDoubles(0, 1, 10, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(0, 6,  2, 1, 1, 8, true));
                voxels.add(new VoxelDoubles(0, 1,  1, 1, 6, 1, true));
                voxels.add(new VoxelDoubles(0, 1,  2, 1, 1, 8, true));
                // square center for hidden part
                voxels.add(new VoxelDoubles(0, 3,  5, 1, 2, 2, true));
            } else {
                //vertical grid
                voxels.add(new VoxelDoubles(1, 0, 8, 2,  7, 2, true));
                voxels.add(new VoxelDoubles(1, 0, 4, 2, 11, 2, true));
                voxels.add(new VoxelDoubles(1, 0, 0, 2, 16, 2, true));
                //diagonal grid
                for (int i = 2; i < 12; i++) {
                    voxels.add(new VoxelDoubles(1, 14 - i, i, 2, 2, 1, true));
                }
            }
        }

        //for right, we handle symmetry
        if (!placing.isLeft()){
            int len = voxels.size();
            for (int i=0;i<len;i++){
                VoxelDoubles oldVoxel = voxels.get(i);
                VoxelDoubles symVoxel = oldVoxel.makeSymmetry(Direction.Axis.X, Direction.Axis.Y);
                voxels.set(i,symVoxel);
            }
        }
        return getShapeFromVoxelIntsTab(voxels,facing,VoxelShapes.empty());
    }

    public static VoxelShape makeDrawBridgeShape(DrawBridgePosition position,int animState,Direction facing){
        //totally closed part for existing part
        if (animState == 0){
            VoxelDoubles flatSquareVoxel = new VoxelDoubles(0,0,14,16,16,2,true); //voxel-shape for anim=0
            return flatSquareVoxel.rotate(Direction.SOUTH,facing).getAssociatedShape();
        }
        List<VoxelDoubles> voxels=Lists.newArrayList();
        int begX = 0;
        //add common support
        if (position.isRight()){
            voxels.add(new VoxelDoubles(0, 0,14, 4,16,2,true)); //right
            begX = 4;
        }else{
            voxels.add(new VoxelDoubles(12, 0,14,4,16,2,true)); //left
        }
        //add the top of the door
        if (position.isUpUp()){
            voxels.add(new VoxelDoubles(0, 12,14,16, 4,2,true));
        }
        //add the bridge
        double deltaZ=Math.tan(Math.PI/8*animState);
        int endY=MathHelper.fastFloor(40*Math.cos(animState*Math.PI/8));
        if (!position.isUp()){
            if (animState==4){
                voxels.add(new VoxelDoubles(begX,0,16,12,2,16,true)); //open bridge
            }else {
                for (int i = 0; i <= endY; i++) { //for part in diagonal
                    voxels.add(new VoxelDoubles(begX, i, 13.8 + i * deltaZ, 12, 1, 2, true));
                }
            }
        }
        //merge all vi for voxel-shape
        VoxelShape shape = VoxelShapes.empty();
        for (VoxelDoubles vi : voxels){
            shape = VoxelShapes.or(shape, vi.rotate(Direction.SOUTH, facing).getAssociatedShape());
        }
        return shape;
    }

    public static boolean isNonNullAndNotEqual(@Nullable Object serverPrice, Object clientPrice){
        return Objects.nonNull(serverPrice) && !Objects.equals(serverPrice,clientPrice);
    }


}
