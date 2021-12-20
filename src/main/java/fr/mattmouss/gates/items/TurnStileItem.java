package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.TurnStile;
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

public class TurnStileItem extends BlockItem {
    public TurnStileItem(TurnStile stile) {
        super(stile, new Item.Properties().tab(ModSetup.itemGroup));
        this.setRegistryName("turn_stile");
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockItemUseContext biuContext = new BlockItemUseContext(context);
        BlockPos pos = biuContext.getClickedPos();
        World world = biuContext.getLevel();
        PlayerEntity entity = biuContext.getPlayer();
        ActionResultType actionResultType = super.useOn(context);
        TurnStileKeyItem key = (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem();
        if (actionResultType == ActionResultType.SUCCESS){
            System.out.println("block successfully put !!");
            ItemStack newStack = new ItemStack(key);
            // the position referred in the turn stile key is the Control Unit ( which is the first pos where we place a block)
            key.setTSPosition(newStack,world,pos);
            assert entity != null;
            Functions.moveMainOldStackToFreeSlot(entity);
            System.out.println("item stack of new key item : "+newStack);
            entity.setItemSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemSlot(EquipmentSlotType.MAINHAND,newStack);
            Networking.INSTANCE.sendToServer(new PacketReplaceBlockItemByKey(pos,true,entity.getUUID()));
            return super.useOn(context);
        }
        System.out.println("block not created");
        return ActionResultType.FAIL;
    }


}
