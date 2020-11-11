package fr.mattmouss.gates.windows;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public enum WindowPlace implements IStringSerializable {
    //a one block window
    FULL(0,"full",0x0000),
    //a two block window
    //with vertical disposition
    BOTH_UP(1,"both_up",0x1000),
    BOTH_DOWN(2,"both_down",0x0100),
    //with horizontal disposition
    BOTH_RIGHT(3,"both_right",0x0001),
    BOTH_LEFT(4,"both_left",0x0010),
    //a three block window
    //with vertical disposition
    BOTH_MIDDLE(5,"both_middle",0x1100),
    //a four block window as square
    UP_RIGHT(6,"up_right",0x1001),
    UP_LEFT(7,"up_left",0x1010),
    DOWN_RIGHT(8,"down_right",0x0101),
    DOWN_LEFT(9,"down_left",0x0110),
    //a six block window with handle in the center
    MIDDLE_RIGHT(10,"middle_right",0x1101),
    MIDDLE_LEFT(11,"middle_left",0x1110);


    private final String name;
    private final int meta;
    //flag = UDLR
    //U = isUpPlace
    //D = isDownPlace
    //L = isLeftPlace
    //R = isRightPlace
    private final int flag;

    WindowPlace(int meta, String name,int flag){
        this.meta = meta;
        this.name = name;
        this.flag = flag;
    }

    public int getMeta() {
        return meta;
    }

    public boolean isUpPlace(){
        return (flag & 0x1000) == 0x1000;
    }

    public boolean isDownPlace(){
        return (flag & 0x0100) == 0x0100;
    }

    public boolean isLeftPlace(){
        return (flag & 0x0010) == 0x0010;
    }

    public boolean isRightPlace(){
        return (flag & 0x0001)==0x0001;
    }

    public boolean isCorner(){
        return (meta>5 && meta<10);
    }

    public WindowPlace getOpposite(){
        if (this.isCorner()){
            switch (this){
                case DOWN_LEFT:
                    return UP_RIGHT;
                case UP_LEFT:
                    return DOWN_RIGHT;
                case UP_RIGHT:
                    return DOWN_LEFT;
                case DOWN_RIGHT:
                    return UP_LEFT;
                default:
                    return null;
            }
        }else {
            return WindowPlace.FULL;
        }
    }



    public static WindowPlace getFromNeighboring(World world, BlockPos pos, BlockState state,@Nullable Direction future_facing) {
        //we get direction where there is not a window block
        List<Direction> notWindowNeighBorFound = getNonWindowNeighbor(world,pos,state,future_facing);
        Places places = new Places();
        Direction facing;
        if (future_facing == null){
            facing= state.get(BlockStateProperties.HORIZONTAL_FACING);
        }else {
            facing = future_facing;
        }

        for (Direction dir : notWindowNeighBorFound){
            //we search in all this direction
            switch (dir){
                case DOWN:
                    //no window underneath this block : remove up block
                    places.filter(WindowPlace::isUpPlace);
                    break;
                case UP:
                    //no window above this block : remove up block
                    places.filter(WindowPlace::isDownPlace);
                    break;
                case NORTH:
                    //the facing direction is the normal to the surface of window in the direction of the player after the placing of the block
                    //two case that are to control : if NORTH is not in the frontal axis
                    places.filter(windowPlace -> (facing == Direction.EAST) ? windowPlace.isLeftPlace() : windowPlace.isRightPlace());
                    break;
                case SOUTH:
                    places.filter(windowPlace -> (facing == Direction.EAST) ? windowPlace.isRightPlace() : windowPlace.isLeftPlace());
                    break;
                case WEST:
                    places.filter(windowPlace -> (facing == Direction.SOUTH) ? windowPlace.isRightPlace() : windowPlace.isLeftPlace());
                    break;
                case EAST:
                    places.filter(windowPlace -> (facing == Direction.SOUTH) ? windowPlace.isLeftPlace() : windowPlace.isRightPlace());
                    break;
            }
        }
        return getWindowPlaceFromPlaces(places,world,pos,state,facing);
    }

    private static WindowPlace getWindowPlaceFromPlaces(Places places,World world,BlockPos pos,BlockState state,Direction facing){
        //all filter occur
        if (places.getSize() == 1){
            //we get the only windowPlace remaining
            return places.getWindowPlace();
        }
        //three filter occur
        if (places.getSize() == 2){
            //we have only one possible value with full
            places.filter(windowPlace -> (windowPlace == WindowPlace.FULL));
            //we have only a place of size one
            return getWindowPlaceFromPlaces(places,world,pos,state,facing);
        }
        //two filter occur except for left and right (give then full)
        if (places.getSize() == 4){
            //we get the window Place that are complex (with two or more true statement (here it is only one value))
            Places complexPlaces = places.getPlace(windowPlace -> windowPlace.getMeta()>4);
            WindowPlace complexPlace = WindowPlace.FULL;
            if (complexPlaces.getSize() == 1){
                //standard verification
                complexPlace = complexPlaces.getWindowPlace();
            }else {
                System.out.println("Complexe places that is not working : "+complexPlaces);
                complexPlaces.forEach(windowPlace -> {
                    System.out.println("value in the list :"+windowPlace);
                });
                System.out.println("Places that is not working : "+places);
            }
            //we check the presence of the block (if WP = BOTH_MIDDLE return true)
            if (complexPlace.checkValidity(world,pos,state,facing)){
                return complexPlace;
            }
            //if it is not valid we get the only both that is on vertical line
            places.filter(windowPlace -> (windowPlace.getMeta()>=3 || windowPlace ==WindowPlace.FULL));
            return getWindowPlaceFromPlaces(places,world,pos,state,facing);
        }

        //when we filter only once we take away 1 both 2 corner
        //when we filter only on UP or DOWN, we take away the three middle value
        //this lead to 6 in size.
        if (places.getSize() == 6){
            Places complexPlaces = places.getPlace(windowPlace -> windowPlace.getMeta()>5);
            int flag = getValidityFlag(complexPlaces,world,pos,state,facing);
            //nothing is valid
            if ((flag & 0x01)== 0x01){
                //arbitrary choice
                return complexPlaces.getWindowPlace();
                //if the first windowPlace is not valid
            }else {
                WindowPlace unvalidPlace =complexPlaces.getWindowPlace();
                //it filter left or right place depending on the unvalidPlace flag (if it is right we filter right and same for left)
                places.filter(windowPlace -> (windowPlace.flag & (unvalidPlace.flag & 0x0011) ) == (unvalidPlace.flag& 0x0011));
                return getWindowPlaceFromPlaces(places, world, pos, state,facing);
            }
        }

        //when we filter only on LEFT or RIGHT, we take away only one middle instead of three
        //this lead to a size of 8
        if (places.getSize() == 8){
            //we get the two corner place
            Places cornerPlaces = places.getPlace(windowPlace -> (windowPlace.getMeta()>5 && windowPlace.getMeta()<10));
            WindowPlace unvalidCorner = getUnvalidCorner(cornerPlaces,world,pos,state,true,facing);
            //if the two corner are valid
            if (unvalidCorner == WindowPlace.FULL){
                //we get the middle left or right place
                return places.getPlace(windowPlace -> windowPlace.getMeta()>9).getWindowPlace();
                //if the first one is not valid (and not the second one)
            }else if (unvalidCorner != null){
                //it filter up or down place depending on the unValidCorner flag (if it is down we filter down and same for up)
                places.filter(windowPlace -> (windowPlace.flag & (unvalidCorner.flag & 0x1100) ) == (unvalidCorner.flag & 0x1100));
                return getWindowPlaceFromPlaces(places,world,pos,state,facing);
            }else {
                //unvalidCorner is null if the two are both invalid which means that we cannot know which one we need to return
                return BOTH_MIDDLE;
            }
        }
        //nothing has been filtered
        if (places.getSize() == 12){
            Places cornerPlaces = places.getPlace(windowPlace -> (windowPlace.getMeta()>5 && windowPlace.getMeta()<10));
            WindowPlace unvalidCorner = getUnvalidCorner(cornerPlaces,world,pos,state,false,facing);
            //it filter left or right place depending on the unvalidPlace flag (if it is right we filter right and same for left)
            //because we need to remove middle_right or left when impossible
            places.filter(windowPlace -> (windowPlace.flag & (unvalidCorner.flag & 0x0011) ) == (unvalidCorner.flag& 0x0011));
            return getWindowPlaceFromPlaces(places, world, pos, state,facing);
        }
        return WindowPlace.FULL;
    }

    //flag is a boolean stocker
    //flag = 0xAnA(n-1)...A0 where Ai = (is the i-th WindowPlace valid)
    private static int getValidityFlag(Places cornerPlaces,World world,BlockPos pos,BlockState state,Direction facing){
        AtomicInteger flag = new AtomicInteger(0);
        AtomicInteger incr = new AtomicInteger(0);
        cornerPlaces.forEach(windowPlace -> {
            if (windowPlace.checkValidity(world,pos,state,facing)){
                flag.addAndGet((int) Math.pow(2.0,incr.get()));
            }
            incr.incrementAndGet();
        });
        return flag.get();
    }

    //we will use this function to notify nearby block of changement in this window Block
    //the direction offset of block nearby are stocked within a base 6 integer
    // i = (abcd...)6 where every a b c d represent a direction index (which is between 0 and 5)
    public List<WindowDirection> getDirectionOfChangingWindow(Direction facing, World world, BlockPos pos){
        List<WindowDirection> directions = new ArrayList<>();
        if (this.isUpPlace()){
            directions.add(new WindowDirection(1,Direction.DOWN));
        }
        if (this.isDownPlace()){
            directions.add(new WindowDirection(1,Direction.UP));
        }
        if (this.isRightPlace()){
            directions.add(new WindowDirection(1,facing.rotateY()));
        }
        if (this.isLeftPlace()){
            directions.add(new WindowDirection(1,facing.rotateYCCW()));
        }
        Direction HorDir = (isRightPlace()) ? facing.rotateY() : facing.rotateYCCW();
        Direction VerDir = (isUpPlace()) ? Direction.DOWN : Direction.UP;
        if (this.isCorner()){
            BlockState state = world.getBlockState(pos.offset(VerDir));
            boolean flag = false;
            if (state.has(WindowBlock.WINDOW_PLACE)){
                flag = (state.get(WindowBlock.WINDOW_PLACE).isMiddle());
            }
            //we add the opposite corner
            directions.add(new WindowDirection(1,HorDir,1,VerDir));
            //the flag specify if we need to add the changement of window block 2 blocks away
            if (flag){
                //we add the 2 way up/down
                directions.add(new WindowDirection(2,VerDir));
                //we add the 2 way up/down and one way right/left
                directions.add(new WindowDirection(2,VerDir,1,HorDir));
            }
        }
        //todo : if possible change the way of getting position and allow us to add offset when multiple middle block
        if (this.isMiddle()){
            directions.add(new WindowDirection(1,HorDir,1, Direction.UP));
            directions.add(new WindowDirection(1,HorDir,1,Direction.DOWN));
            int i =1;
            //a code that allow people to make extra high windows
            //if there is middle in up direction
            while (world.getBlockState(pos.offset(Direction.UP,i)).get(WindowBlock.WINDOW_PLACE).isMiddle() && i<32){
                directions.add(new WindowDirection(1,HorDir,i+1,Direction.UP));
                directions.add(new WindowDirection(i+1, Direction.UP));
                i++;
            }
            i=1;
            //if there is middle in down direction
            while (world.getBlockState(pos.offset(Direction.DOWN,i)).get(WindowBlock.WINDOW_PLACE).isMiddle() && i<32){
                directions.add(new WindowDirection(1,HorDir,i+1,Direction.DOWN));
                directions.add(new WindowDirection(i+1,Direction.DOWN));
                i++;
            }
        }
        return directions;

    }

    private boolean isMiddle() {
        return (meta > 9);
    }


    //give the unvalid corner if it exist, full if not
    //return null if multiple when boolean is true
    private static WindowPlace getUnvalidCorner(Places cornerPlaces, World world,BlockPos pos,BlockState state,boolean ifMultipleReturnNull,Direction facing){
        AtomicReference<WindowPlace> unValidCorner = new AtomicReference<>(WindowPlace.FULL);
        AtomicInteger number_of_def = new AtomicInteger(0);
        cornerPlaces.forEach(windowPlace -> {
            if (!windowPlace.checkValidity(world,pos,state,facing)){
                unValidCorner.set(windowPlace);
                number_of_def.incrementAndGet();
            }
        });
        if (number_of_def.get() <= 1 || !ifMultipleReturnNull){
            return unValidCorner.get();
        }else {
            //the two are invalid
            return null;
        }
    }

    //this function will check if the corner at the opposite position is really there

    private boolean checkValidity(World world, BlockPos pos, BlockState state,Direction facing) {
        if (this == WindowPlace.BOTH_MIDDLE){
            return true;
        }
        if (this.isCorner()){
            WindowPlace oppositeCorner = this.getOpposite();
            Direction verDir,horDir;
            if (oppositeCorner.isUpPlace()){
                verDir = Direction.UP;
            }else {
                verDir = Direction.DOWN;
            }
            //facing direction is from block to position of player
            if (oppositeCorner.isRightPlace()){
                horDir = facing.rotateYCCW();
            }else {
                horDir = facing.rotateY();
            }
            //we check
            WindowBlock corner_Block = (WindowBlock) world.getBlockState(pos).getBlock();
            Block other_corner_Block = world.getBlockState(pos.offset(horDir).offset(verDir)).getBlock();
            return (corner_Block.equals(other_corner_Block));
        }else {
            return false;
        }
    }




    private static List<Direction> getNonWindowNeighbor(World world, BlockPos pos, BlockState state,Direction future_facing) {
        Direction.Axis FrontalAxis;
        if (future_facing == null) {
            FrontalAxis = state.get(BlockStateProperties.HORIZONTAL_FACING).getAxis();
        }else {
            FrontalAxis = future_facing.getAxis();
        }
        List<Direction> NeighborDirs = new ArrayList<>();
        for (Direction direction : Direction.values()){
            if (direction.getAxis() != FrontalAxis){
                Block block = world.getBlockState(pos.offset(direction)).getBlock();
                if (!(block instanceof WindowBlock)){
                    NeighborDirs.add(direction);
                }
            }
        }
        return NeighborDirs;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public BlockPos getRandNeighborPos(BlockPos pos,Direction facing) {
        for (int i=0;i<4;i++) {
            int flag2 = (flag>>4*i);
            if ((flag2& 1)== 1){
                //we found a neighbor
                Direction offsetDir = (i==2)? Direction.UP : Direction.DOWN;
                if (i<2){
                    offsetDir = (i==0)? facing.rotateY() : facing.rotateYCCW();
                }
                return pos.offset(offsetDir);
            }
        }
        return null;
    }


    public static class Places{
        private List<WindowPlace> places;

        public Places(){
            WindowPlace[] places_array = WindowPlace.values();
            places = new ArrayList<>(Arrays.asList(places_array));
        }

        public Places(List<WindowPlace> windowPlaces){
            places = windowPlaces;
        }

        //this function is removing window place that satisfy the predicate
        public void filter(Predicate<WindowPlace> func){
            List<WindowPlace> removedPlace = new ArrayList<>();
            for (WindowPlace wp : places){
                if (func.test(wp)){
                    removedPlace.add(wp);
                }
            }
            for (WindowPlace wp : removedPlace){
                places.remove(wp);
            }
        }

        public Places getPlace(Predicate<WindowPlace> func){
            ArrayList<WindowPlace> rightPlace = new ArrayList<>();
            for (WindowPlace wp : places){
                if (func.test(wp)){
                    rightPlace.add(wp);
                }
            }
            return new Places(rightPlace);
        }

        public void forEach(Consumer<? super WindowPlace> consumer){
            places.forEach(consumer);
        }


        public int getSize(){
            return places.size();
        }

        public WindowPlace getWindowPlace(){
            return places.get(0);
        }

    }


}
