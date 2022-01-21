package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;

public class RedstoneTollGateTileEntity extends AbstractTollGateTileEntity implements ITickableTileEntity {
    private boolean lastPowered = false;
    private boolean initialise = false;

    public RedstoneTollGateTileEntity() {
        super(ModBlock.REDSTONE_TOLL_GATE_ENTITY_TYPE);
    }

    @Override
    public void tick() {
        assert level != null;
        if (!level.isClientSide) {
            BlockState state = this.getBlockState();
            if (!initialise){
                initialise = true;
                lastPowered = state.getValue(BlockStateProperties.POWERED);
            }
            if (lastPowered != state.getValue(BlockStateProperties.POWERED)) {
                startAllAnimation();
            }
            manageAnimation();
            lastPowered = state.getValue(BlockStateProperties.POWERED);
        }
    }


}
