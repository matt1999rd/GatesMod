package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.blocks.ModBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;;

public class RedstoneTollGateTileEntity extends AbstractTollGateTileEntity {
    private boolean lastPowered = false;
    private boolean initialise = false;

    public RedstoneTollGateTileEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlock.REDSTONE_TOLL_GATE_ENTITY_TYPE,blockPos,blockState);
    }

    public void tick(Level level,BlockState state) {
        if (!level.isClientSide) {
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
