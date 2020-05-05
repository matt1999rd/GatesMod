package fr.mattmouss.gates.items;

import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class TurnStileKeyItem extends KeyItem {
    public TurnStileKeyItem() {
        super();
        this.setRegistryName("turn_stile_key");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        PlayerEntity entity = context.getPlayer();
        World world = context.getWorld();
        Hand hand = context.getHand();
        ItemStack stack = entity.getHeldItem(hand);
        TileEntity te= world.getTileEntity(pos);
        if (!(te instanceof TurnStileTileEntity)){
            //we exit the function if it is not a TollGateTileEntity
            return super.onItemUse(context);
        }

        //if someone takes the key from the creative tab it will not have any BlockPos and we set the blockPos to the present turn stile
        //if this key has a blockPos that don't correspond to a turn stile anymore (because of destroyed block) we set the blockPos also
        BlockPos registeredPos = getTSPosition(stack,world);
        if (registeredPos == null){
            setTSPosition(stack,world,pos);
            registeredPos = getTSPosition(stack,world);
        }
        if (!pos.equals(registeredPos)){
            System.out.println("the registered pos is not the pos of this block");
            System.out.println("pos of turn stile key attribute :"+registeredPos);
            System.out.println("pos of turn stile :"+pos);
            return super.onItemUse(context);
        }
        TurnStileTileEntity tste = (TurnStileTileEntity) world.getTileEntity(pos);
        if (!world.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) entity, tste, tste.getPos());
        }
        return super.onItemUse(context);

    }


}
