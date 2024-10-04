package fr.moonshade.gates.tileentity;


import fr.moonshade.gates.blocks.ModBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneTurnStileTileEntity extends AbstractTurnStileTileEntity {

    public RedstoneTurnStileTileEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlock.REDSTONE_TURNSTILE_TILE_TYPE,blockPos,blockState);
    }

    public void tick() {
        if (this.isControlUnit()) {
            managePlayerMovement();
        }
    }

}
