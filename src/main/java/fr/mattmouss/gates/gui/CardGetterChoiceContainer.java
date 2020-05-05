package fr.mattmouss.gates.gui;


import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class CardGetterChoiceContainer extends Container {
    private CardGetterTileEntity tileEntity ;
    private PlayerEntity playerEntity;


    public CardGetterChoiceContainer(int windowId, World world, BlockPos pos,PlayerEntity player) {
        super(ModBlock.CARD_GETTER_CHOICE_CONTAINER, windowId);
        tileEntity = (CardGetterTileEntity) world.getTileEntity(pos);
        playerEntity = player;
    }

    public void raisePrice(){
        tileEntity.raisePrice();
    }

    public void lowerPrice(){
        tileEntity.lowerPrice();
    }

    public CardGetterTileEntity getTileEntity(){
        return tileEntity;
    }

    public int getPrice(){
        return tileEntity.getPrice();
    }

    public int getId(){
        return tileEntity.getKeyID();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(
                IWorldPosCallable.of(tileEntity.getWorld(),tileEntity.getPos()),
                playerEntity,
                ModBlock.CARD_GETTER
        );
    }
}
