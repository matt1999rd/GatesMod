package fr.mattmouss.gates.items;

import fr.mattmouss.gates.tileentity.IControlIdTE;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
        System.out.println("on item use !!");
        BlockPos pos = context.getPos();
        PlayerEntity entity = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack stack = entity.getHeldItem(hand);
        World world = context.getWorld();
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IControlIdTE)) {
            //we exit the function if it is not a TollGateTileEntity or a TurnStileTileEntity
            System.out.println("not the right tile entity : "+te);
            return super.onItemUse(context);
        }
        int te_id = ((IControlIdTE)te).getId();
        CompoundNBT tag = stack.getTag();
        //if id is not existing we fix it
        if (tag == null){
            tag = new CompoundNBT();
        }
        if (!tag.contains("te")) {
            System.out.println("registering id of block");
            if (te instanceof TollGateTileEntity) {
                tag.putInt("id", te_id);
                tag.putString("te", "toll_gate");
            } else {
                tag.putInt("id",te_id);
                tag.putString("te", "turn_stile");
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        //this first five line is a filter for when stack has no id stored
        int id = getId(stack);
        if (id == -1){
            return super.getDisplayName(stack);
        }
        return new TranslationTextComponent(this.getTranslationKey(stack)+" nb "+id+" for "+getGateClass(stack));
    }

    public int getId(ItemStack stack) {
        CompoundNBT nbt =stack.getTag();
        if (nbt != null && nbt.contains("id")){
            return nbt.getInt("id");
        }
        return -1;
    }

    public String getGateClass(ItemStack stack) {
        CompoundNBT nbt =stack.getTag();
        if (nbt != null && nbt.contains("te")){
            return nbt.getString("te");
        }
        return "";
    }
}
