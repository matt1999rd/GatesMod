package fr.mattmouss.gates.items;

import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.tileentity.IControlIdTE;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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



public class CardKeyItem extends Item {

    public CardKeyItem(){
        super(new Item.Properties().group(ModSetup.itemGroup).maxStackSize(1));
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
        //if the clicked block is part of the toll gate that is not CU
        if (te_id == -1){
            return super.onItemUse(context);
        }
        CompoundNBT tag = stack.getTag();
        //if id is not existing we fix it
        if (tag == null){
            tag = new CompoundNBT();
        }
        if (!tag.contains("id")) {
            System.out.println("registering id of block");
            tag.putInt("id", te_id);
            stack.setTag(tag);
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
        String formatedString = "# %1$d";
        String st =String.format(formatedString,id);
        return new TranslationTextComponent(this.getDefaultTranslationKey(),st);
    }


    public int getId(ItemStack stack) {
        CompoundNBT nbt =stack.getTag();
        if (nbt != null && nbt.contains("id")){
            return nbt.getInt("id");
        }
        return -1;
    }

}
