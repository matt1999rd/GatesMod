package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.DrawBridge;
import fr.mattmouss.gates.enum_door.DrawBridgePosition;
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
    public DrawBridgeTileEntity() {
        super(ModBlock.DRAW_BRIDGE_TILE_TYPE);
    }

    @Override
    public void tick() {
        BlockState state = this.getBlockState();
        Block block=state.getBlock();
        if (!(block instanceof DrawBridge))return;
        if (state.get(POSITION) == DOOR_LEFT_DOWN) {
            if (!world.isRemote){
                if (!initialise){
                    initialise = true;
                    lastPowered = state.get(BlockStateProperties.POWERED);
                }
                if (lastPowered != state.get(BlockStateProperties.POWERED)) {
                    if (lastPowered){ //unpowered draw bridge
                        isClosing = true;
                    }else { //powered draw bridge
                        isOpening = true;
                    }
                }
                int animState=state.get(ANIMATION);
                if (isOpening){
                    if (animState == 4){
                        isOpening = false;
                    }else {
                        changeAnim(true,animState+1);
                    }
                }else if (isClosing){
                    if (animState == 0){
                        isClosing = false;
                    }else {
                        changeAnim(false,animState-1);
                    }
                }
            }
        }
        lastPowered = state.get(BlockStateProperties.POWERED);
    }

    private void changeAnim(boolean isOpening,int futureAnimState){
        BlockState state = getBlockState();
        if (state.get(POSITION) != DOOR_LEFT_DOWN)return;
        Block block = state.getBlock();
        if (!(block instanceof DrawBridge))return;
        DrawBridge bridge = (DrawBridge)block;
        List<BlockPos> posList= bridge.getNeighborPositions(state.get(BlockStateProperties.HORIZONTAL_FACING),pos,DOOR_LEFT_DOWN);
        for (BlockPos neiPos : posList){
            BlockState neiState=world.getBlockState(neiPos);
            world.setBlockState(neiPos,neiState.with(BlockStateProperties.POWERED,isOpening).with(ANIMATION,futureAnimState));
        }
    }

}
