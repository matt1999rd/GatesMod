package fr.mattmouss.gates.items;

import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public class CardKeyItem extends KeyItem {

    //TODO :  change the system of authentification : make an id on each TollGate and each TurnStile for cardkeyitem (position registering kept for use in techKeyItem)
    //TODO : add a system of registering the control id already used (in process)

    public CardKeyItem(){
        super();
        this.setRegistryName("card_key");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        PlayerEntity entity = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack stack = entity.getHeldItem(hand);
        World world = context.getWorld();
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TollGateTileEntity) && (!(te instanceof TurnStileTileEntity))) {
            //we exit the function if it is not a TollGateTileEntity or a TurnStileTileEntity
            return super.onItemUse(context);
        }
        BlockPos te_pos = te.getPos();
        CompoundNBT tag = stack.getOrCreateTag();
        if (!this.isTGPositionReg(tag, world) || !this.isTSPositionReg(tag,world)) {
            if (te instanceof TollGateTileEntity) {
                te_pos =
                System.out.println("registering pos of block");
                tag.putInt("pos_x", te_pos.getX());
                tag.putInt("pos_y", te_pos.getY());
                tag.putInt("pos_z", te_pos.getZ());
                tag.putString("te", "toll_gate");
            } else {
                tag.putString("te", "turn_stile");
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }
}
