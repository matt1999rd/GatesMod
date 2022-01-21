package fr.mattmouss.gates.tileentity;


import fr.mattmouss.gates.blocks.ModBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class RedstoneTurnStileTileEntity extends AbstractTurnStileTileEntity implements ITickableTileEntity {

    private boolean isAnimationInWork = false;
    private boolean lastPowered = false;
    private boolean initialise = false;

    public RedstoneTurnStileTileEntity() {
        super(ModBlock.REDSTONE_TURNSTILE_TILE_TYPE);
    }

    @Override
    public void tick() {
        BlockState state = this.getBlockState();
        if (this.isControlUnit()) {
            assert level != null;
            if (!level.isClientSide){
                if (!initialise){
                    initialise = true;
                    lastPowered = state.getValue(BlockStateProperties.POWERED);
                }
                if (lastPowered != state.getValue(BlockStateProperties.POWERED)) {
                    openOrBlockAllTS();
                }
            }
            managePlayerMovement();
        }
        lastPowered = state.getValue(BlockStateProperties.POWERED);
    }

    //other function for functionality in the te

    private void openOrBlockAllTS() {
        List<BlockPos> posList=this.getPositionOfBlockConnected();
        for (BlockPos pos:posList){
            assert level != null;
            TileEntity te=level.getBlockEntity(pos);
            if (te instanceof RedstoneTurnStileTileEntity){
                RedstoneTurnStileTileEntity rtste=(RedstoneTurnStileTileEntity)te;
                if (lastPowered){
                    rtste.blockTS();
                }else {
                    rtste.openTS();
                }
            }
        }
    }

    protected boolean isAnimationInWork() {
        return isAnimationInWork;
    }

    protected void startAnimation() {
        isAnimationInWork = true;
    }

    protected void endAnimation() {
        isAnimationInWork = false;
    }



}
