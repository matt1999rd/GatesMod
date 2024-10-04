package fr.moonshade.gates.tileentity;

import fr.moonshade.gates.blocks.ModBlock;
import fr.moonshade.gates.doors.DrawBridge;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import java.util.List;

import static fr.moonshade.gates.doors.DrawBridge.ANIMATION;
import static fr.moonshade.gates.doors.DrawBridge.POSITION;
import static fr.moonshade.gates.enum_door.DrawBridgePosition.DOOR_LEFT_DOWN;

public class DrawBridgeTileEntity extends BlockEntity {

    private boolean isOpening =   false;
    private boolean isClosing =   false;
    private boolean lastPowered = false;
    private boolean initialise =  false;
    private int ticksGap =10;

    public DrawBridgeTileEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlock.DRAW_BRIDGE_TILE_TYPE,blockPos,blockState);
    }


    public void tick(BlockState state) {
        Block block=state.getBlock();
        if (!(block instanceof DrawBridge))return;
        if (state.getValue(POSITION) == DOOR_LEFT_DOWN) {
            assert level != null;
            if (!level.isClientSide){
                if (!initialise){
                    initialise = true;
                    lastPowered = state.getValue(BlockStateProperties.POWERED);
                }
                if (lastPowered != state.getValue(BlockStateProperties.POWERED)) {
                    if (lastPowered){ //unpowered draw bridge
                        isClosing = true;
                    }else { //powered draw bridge
                        isOpening = true;
                    }
                }
                int animState=state.getValue(ANIMATION);
                if (isOpening){
                    if (animState == 4){
                        isOpening = false;
                    }else {
                        if (ticksGap==0){
                            changeAnim(true,animState+1);
                            ticksGap = 10;
                        }
                        ticksGap--;
                    }
                }else if (isClosing){
                    if (animState == 0){
                        isClosing = false;
                    }else {
                        if (ticksGap == 0){
                            changeAnim(false,animState-1);
                            ticksGap = 10;
                        }
                        ticksGap--;
                    }
                }
            }
        }
        lastPowered = state.getValue(BlockStateProperties.POWERED);
    }

    private void changeAnim(boolean isOpening,int futureAnimState){
        BlockState state = getBlockState();
        if (state.getValue(POSITION) != DOOR_LEFT_DOWN)return;
        Block block = state.getBlock();
        if (!(block instanceof DrawBridge))return;
        DrawBridge bridge = (DrawBridge)block;
        List<BlockPos> posList= bridge.getNeighborPositions(state.getValue(BlockStateProperties.HORIZONTAL_FACING),worldPosition,DOOR_LEFT_DOWN);
        for (BlockPos neiPos : posList){
            assert level != null;
            BlockState neiState=level.getBlockState(neiPos);
            if (!(neiState.getBlock() instanceof AirBlock))level.setBlockAndUpdate(neiPos,neiState.setValue(BlockStateProperties.POWERED,isOpening).setValue(ANIMATION,futureAnimState));
        }
    }

}
