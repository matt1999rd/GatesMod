package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BushBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TollGateItem extends BlockItem {
    public TollGateItem(TollGate gate) {
        super(gate, new Item.Properties().group(ModSetup.itemGroup));
        this.setRegistryName("toll_gate");
    }


    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos =context.getPos();
        World world = context.getWorld();
        PlayerEntity entity=context.getPlayer();
        ActionResultType actionResultType = super.onItemUse(context);
        TollKeyItem key = (TollKeyItem) ModItem.TOLL_GATE_KEY.asItem();
        System.out.println("item defini : "+key);
        if (actionResultType == ActionResultType.SUCCESS){
            ItemStack newStack = new ItemStack(key);
            key.setTGPosition(newStack,world,pos.up());
            System.out.println("itemstack of new key item : "+newStack);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,newStack);
            return super.onItemUse(context);
        }
        System.out.println("block non fabriqué");
        return actionResultType;
    }


}
