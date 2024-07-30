package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.TurnStile;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketReplaceBlockItemByKey;
import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;

public class TurnStileItem extends BlockItem {
    public TurnStileItem(TurnStile stile) {
        super(stile, new Item.Properties().tab(ModSetup.itemGroup));
        this.setRegistryName("turn_stile");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext biuContext = new BlockPlaceContext(context);
        BlockPos pos = biuContext.getClickedPos();
        Level world = biuContext.getLevel();
        Player entity = biuContext.getPlayer();
        InteractionResult actionResultType = super.useOn(context);
        TurnStileKeyItem key = (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem();
        if (actionResultType == InteractionResult.SUCCESS){
            System.out.println("block successfully put !!");
            ItemStack newStack = new ItemStack(key);
            // the position referred in the turn stile key is the Control Unit ( which is the first pos where we place a block)
            key.setTSPosition(newStack,world,pos);
            assert entity != null;
            Functions.moveMainOldStackToFreeSlot(entity);
            System.out.println("item stack of new key item : "+newStack);
            entity.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlot.MAINHAND,newStack);
            Networking.INSTANCE.sendToServer(new PacketReplaceBlockItemByKey(pos,true,entity.getUUID()));
            return super.useOn(context);
        }
        System.out.println("block not created");
        return InteractionResult.FAIL;
    }


}
