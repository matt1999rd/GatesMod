package fr.mattmouss.gates.items;

import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.tileentity.IControlIdTE;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;



public class CardKeyItem extends Item {

    public CardKeyItem(){
        super(new Item.Properties().tab(ModSetup.itemGroup).stacksTo(1));
        this.setRegistryName("card_key");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        System.out.println("on item use !!");
        BlockPos pos = context.getClickedPos();
        Player entity = context.getPlayer();
        InteractionHand hand = context.getHand();
        assert entity != null;
        ItemStack stack = entity.getItemInHand(hand);
        Level world = context.getLevel();
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof IControlIdTE)) {
            //we exit the function if it is not a TollGateTileEntity or a TurnStileTileEntity
            System.out.println("not the right tile entity : "+te);
            return super.useOn(context);
        }
        int te_id = ((IControlIdTE)te).getId();
        //if the clicked block is part of the tollgate that is not CU
        if (te_id == -1){
            return super.useOn(context);
        }
        CompoundTag tag = stack.getTag();
        //if id is not existing we fix it
        if (tag == null){
            tag = new CompoundTag();
        }
        if (!tag.contains("id")) {
            System.out.println("registering id of block");
            tag.putInt("id", te_id);
            stack.setTag(tag);
            return InteractionResult.SUCCESS;
        }
        int id = tag.getInt("id");
        //for turn stile we do the test
        if (te instanceof TurnStileTileEntity && ((TurnStileTileEntity) te).getKeyId() == id){
            System.out.println("test of th key");

        }
        return InteractionResult.FAIL;
    }

    @Override
    public Component getName(ItemStack stack) {
        //this first five line is a filter for when stack has no id stored
        int id = getId(stack);
        if (id == -1){
            return super.getName(stack);
        }
        String formattedString = "# %1$d";
        String st =String.format(formattedString,id);
        return new TranslatableComponent(this.getOrCreateDescriptionId(),st);
    }



    public int getId(ItemStack stack) {
        CompoundTag nbt =stack.getTag();
        if (nbt != null && nbt.contains("id")){
            return nbt.getInt("id");
        }
        return -1;
    }



}
