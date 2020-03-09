package fr.mattmouss.gates.gui;

import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class TGTechContainer extends Container {
    private TollGateTileEntity tileEntity ;
    private PlayerEntity playerEntity;
    //private IItemHandler inventory;

    public TGTechContainer(int windowId, World world, BlockPos pos, PlayerEntity entity) {
        super(ModBlock.TOLLGATE_TECH_CONTAINER, windowId);
        tileEntity = (TollGateTileEntity)world.getTileEntity(pos);
        playerEntity =entity;
    }

    public int getRemainingPayment(){
        return tileEntity.getRemainingPayment();
    }

    public TollGateTileEntity getTileEntity() {
        return tileEntity;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(
                IWorldPosCallable.of(tileEntity.getWorld(),tileEntity.getPos()),
                playerEntity,
                ModBlock.TOLL_GATE);
    }
}
