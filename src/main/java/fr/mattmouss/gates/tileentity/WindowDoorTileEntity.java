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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import java.util.List;


public class WindowDoorTileEntity extends TileEntity implements ITickableTileEntity {

    boolean isOpening=false,isClosing=false;

    public WindowDoorTileEntity() {
        super(ModBlock.WINDOW_DOOR_TILE_TYPE);
    }


    @Override
    public void tick() {
        if (this.getBlockState().getValue(WindowDoor.PLACING) == DoorPlacing.CENTER_DOWN) {
            assert level != null;
            if (!level.isClientSide) {
                int animation = getBlockState().getValue(WindowDoor.ANIMATION);
                if (animation == 0 || animation == 4) {
                    ServerWorld world = (ServerWorld) getLevel();
                    boolean existPlayerNearby = false;
                    assert world != null;
                    List<ServerPlayerEntity> playerEntities = world.players();
                    for (ServerPlayerEntity player : playerEntities) {
                        if (!existPlayerNearby) {
                            Vector3d playerPos = player.position();
                            if (playerPos.distanceTo(new Vector3d(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5)) < 3.0F) {
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
