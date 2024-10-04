package fr.moonshade.gates.items;

import fr.moonshade.gates.doors.TollGate;
import fr.moonshade.gates.network.Networking;
import fr.moonshade.gates.network.PacketReplaceBlockItemByKey;
import fr.moonshade.gates.setup.ModSetup;
import fr.moonshade.gates.util.Functions;
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

public class TollGateItem extends BlockItem {
    public TollGateItem(TollGate gate) {
        super(gate, new Item.Properties().tab(ModSetup.itemGroup));
        this.setRegistryName("toll_gate");
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext blockItemUseContext = new BlockPlaceContext(context);
        BlockPos pos = blockItemUseContext.getClickedPos();
        Level world = blockItemUseContext.getLevel();
        Player entity=blockItemUseContext.getPlayer();
        InteractionResult actionResultType = super.useOn(context);
        TollKeyItem key = (TollKeyItem) ModItem.TOLL_GATE_KEY.asItem();
        System.out.println("defined item : "+key);
        if (actionResultType == InteractionResult.SUCCESS){
            ItemStack newStack = new ItemStack(key);
            key.setTGPosition(newStack,world,pos);
            assert entity != null;
            Functions.moveMainOldStackToFreeSlot(entity);
            System.out.println("item stack of new key item : "+newStack);
            entity.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlot.MAINHAND,newStack);
            Networking.INSTANCE.sendToServer(new PacketReplaceBlockItemByKey(pos,false,entity.getUUID()));
        }
        if (actionResultType != InteractionResult.SUCCESS)System.out.println("not created block");
        return actionResultType;
    }


}
