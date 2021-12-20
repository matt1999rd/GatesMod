package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.DrawBridge;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static fr.mattmouss.gates.doors.DrawBridge.ANIMATION;
import static fr.mattmouss.gates.doors.DrawBridge.POSITION;
import static fr.mattmouss.gates.enum_door.DrawBridgePosition.DOOR_LEFT_DOWN;

public class DrawBridgeTileEntity extends TileEntity implements ITickableTileEntity {

    private boolean isOpening =   false;
    private boolean isClosing =   false;
    private boolean lastPowered = false;
    private boolean initialise =  false;
    private int ticksGap =10;
    public DrawBridgeTileEntity() {
        super(ModBlock.DRAW_BRIDGE_TILE_TYPE);
    }

    @Override
    public void tick() {
        BlockState state = this.getBlockState();
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
