package fr.mattmouss.gates.gui;


import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Objects;


public class CardGetterChoiceContainer extends AbstractContainerMenu {
    private final CardGetterTileEntity tileEntity ;
    private final Player playerEntity;


    public CardGetterChoiceContainer(int windowId, Level world, BlockPos pos,Player player) {
        super(ModBlock.CARD_GETTER_CHOICE_CONTAINER, windowId);
        tileEntity = (CardGetterTileEntity) world.getBlockEntity(pos);
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
    public boolean stillValid(Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(Objects.requireNonNull(tileEntity.getLevel()),tileEntity.getBlockPos()),
                playerEntity,
                ModBlock.CARD_GETTER
        );
    }
}
