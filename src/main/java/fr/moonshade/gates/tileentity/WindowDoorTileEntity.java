package fr.moonshade.gates.tileentity;

import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.doors.WindowDoor;
import fr.moonshade.gates.enum_door.DoorPlacing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import java.util.List;


public class WindowDoorTileEntity extends BlockEntity {

    boolean isOpening=false,isClosing=false;

    public WindowDoorTileEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlock.WINDOW_DOOR_TILE_TYPE,blockPos,blockState);
    }



    public void tick(Level level,BlockState state) {
        if (state.getValue(WindowDoor.PLACING) == DoorPlacing.CENTER_DOWN) {
            if (!level.isClientSide) {
                int animation = state.getValue(WindowDoor.ANIMATION);
                if (animation == 0 || animation == 4) {
                    ServerLevel world = (ServerLevel) level;
                    boolean existPlayerNearby = false;
                    List<ServerPlayer> playerEntities = world.players();
                    for (ServerPlayer player : playerEntities) {
                        if (!existPlayerNearby) {
                            Vec3 playerPos = player.position();
                            if (playerPos.distanceTo(new Vec3(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5)) < 3.0F) {
                                existPlayerNearby = true;
                            }
                        }
                    }
                    isOpening = (animation == 0 && existPlayerNearby);
                    isClosing = (animation == 4 && !existPlayerNearby);
                }
                if (isOpening) {
                    openDoor();
                } else if (isClosing) {
                    closeDoor();
                }
            }
        }
    }

    private void openDoor() {
        BlockState state = getBlockState();
        int animationState = state.getValue(WindowDoor.ANIMATION);
        changeAllState(animationState+1,state);
        if (animationState == 3){
            isOpening = false;
        }
    }

    private void closeDoor(){
        BlockState state = getBlockState();
        int animationState = state.getValue(WindowDoor.ANIMATION);
        changeAllState(animationState-1,state);
        if (animationState == 1){
            isClosing = false;
        }
    }

    private void changeAllState(int animationState,BlockState state){
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        assert level != null;
        level.setBlockAndUpdate(worldPosition,state.setValue(WindowDoor.ANIMATION,animationState));
        BlockState state1 = level.getBlockState(worldPosition.above());
        level.setBlockAndUpdate(worldPosition.above(),state1.setValue(WindowDoor.ANIMATION,animationState));
        state1 = level.getBlockState(worldPosition.relative(facing.getClockWise()));
        level.setBlockAndUpdate(worldPosition.relative(facing.getClockWise()),state1.setValue(WindowDoor.ANIMATION,animationState));
        state1 = level.getBlockState(worldPosition.relative(facing.getClockWise()).above());
        level.setBlockAndUpdate(worldPosition.relative(facing.getClockWise()).above(),state1.setValue(WindowDoor.ANIMATION,animationState));
        state1 = level.getBlockState(worldPosition.relative(facing.getCounterClockWise()));
        level.setBlockAndUpdate(worldPosition.relative(facing.getCounterClockWise()),state1.setValue(WindowDoor.ANIMATION,animationState));
        state1 = level.getBlockState(worldPosition.relative(facing.getCounterClockWise()).above());
        level.setBlockAndUpdate(worldPosition.relative(facing.getCounterClockWise()).above(),state1.setValue(WindowDoor.ANIMATION,animationState));
    }
}
