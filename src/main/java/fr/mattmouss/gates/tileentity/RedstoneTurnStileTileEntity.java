package fr.mattmouss.gates.tileentity;


import fr.mattmouss.gates.blocks.ModBlock;
import net.minecraft.tileentity.ITickableTileEntity;

public class RedstoneTurnStileTileEntity extends AbstractTurnStileTileEntity implements ITickableTileEntity {

    public RedstoneTurnStileTileEntity() {
        super(ModBlock.REDSTONE_TURNSTILE_TILE_TYPE);
    }

    @Override
    public void tick() {
        if (this.isControlUnit()) {
            managePlayerMovement();
        }
    }

}
