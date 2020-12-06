package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.WindowDoor;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import java.util.List;


public class WindowDoorTileEntity extends TileEntity implements ITickableTileEntity {

    boolean isOpening=false,isClosing=false;

    public WindowDoorTileEntity() {
        super(ModBlock.WINDOW_DOOR_TILE_TYPE);
    }


    @Override
    public void tick() {
        if (this.getBlockState().get(WindowDoor.PLACING) == DoorPlacing.CENTER_DOWN && !world.isRemote ){
            int animation = getBlockState().get(WindowDoor.ANIMATION);
            if (animation == 0 || animation == 4) {
                ServerWorld world = (ServerWorld) getWorld();
                boolean existPlayerNearby = false;
                List<ServerPlayerEntity> playerEntities = world.getPlayers();
                for (ServerPlayerEntity player : playerEntities) {
                    if (!existPlayerNearby) {
                        Vec3d playerPos = player.getPositionVec();
                        if (playerPos.distanceTo(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) < 3.0F) {
                            existPlayerNearby = true;
                        }
                    }
                }
                isOpening = (animation == 0 && existPlayerNearby);
                isClosing = (animation == 4 && !existPlayerNearby);
            }
            if (isOpening){
                openDoor();
            }else if (isClosing){
                closeDoor();
            }
        }
    }

    private void openDoor() {
        BlockState state = getBlockState();
        int animationState = state.get(WindowDoor.ANIMATION);
        changeAllState(animationState+1,state);
        if (animationState == 3){
            isOpening = false;
        }
    }

    private void closeDoor(){
        BlockState state = getBlockState();
        int animationState = state.get(WindowDoor.ANIMATION);
        changeAllState(animationState-1,state);
        if (animationState == 1){
            isClosing = false;
        }
    }

    private void changeAllState(int animationState,BlockState state){
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        world.setBlockState(pos,state.with(WindowDoor.ANIMATION,animationState));
        BlockState state1 = world.getBlockState(pos.up());
        world.setBlockState(pos.up(),state1.with(WindowDoor.ANIMATION,animationState));
        state1 = world.getBlockState(pos.offset(facing.rotateY()));
        world.setBlockState(pos.offset(facing.rotateY()),state1.with(WindowDoor.ANIMATION,animationState));
        state1 = world.getBlockState(pos.offset(facing.rotateY()).up());
        world.setBlockState(pos.offset(facing.rotateY()).up(),state1.with(WindowDoor.ANIMATION,animationState));
        state1 = world.getBlockState(pos.offset(facing.rotateYCCW()));
        world.setBlockState(pos.offset(facing.rotateYCCW()),state1.with(WindowDoor.ANIMATION,animationState));
        state1 = world.getBlockState(pos.offset(facing.rotateYCCW()).up());
        world.setBlockState(pos.offset(facing.rotateYCCW()).up(),state1.with(WindowDoor.ANIMATION,animationState));
    }
}
