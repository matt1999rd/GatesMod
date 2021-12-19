package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.PacketReplaceBlockItemByKey;
import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TollGateItem extends BlockItem {
    public TollGateItem(TollGate gate) {
        super(gate, new Item.Properties().tab(ModSetup.itemGroup));
        this.setRegistryName("toll_gate");
    }


    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockItemUseContext blockItemUseContext = new BlockItemUseContext(context);
        BlockPos pos = blockItemUseContext.getClickedPos();
        World world = blockItemUseContext.getLevel();
        PlayerEntity entity=blockItemUseContext.getPlayer();
        ActionResultType actionResultType = super.useOn(context);
        TollKeyItem key = (TollKeyItem) ModItem.TOLL_GATE_KEY.asItem();
        System.out.println("item defini : "+key);
        if (actionResultType == ActionResultType.SUCCESS){
            ItemStack newStack = new ItemStack(key);
            key.setTGPosition(newStack,world,pos);
            Functions.moveMainOldStackToFreeSlot(entity);
            System.out.println("itemstack of new key item : "+newStack);
            entity.setItemSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlotType.MAINHAND,newStack);
            Networking.INSTANCE.sendToServer(new PacketReplaceBlockItemByKey(pos,false,entity.getUUID()));
        }
        if (actionResultType != ActionResultType.SUCCESS)System.out.println("block non fabriqué");
        return actionResultType;
    }


}
