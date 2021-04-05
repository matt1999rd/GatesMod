package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.TurnStile;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TurnStileItem extends BlockItem {
    public TurnStileItem(TurnStile stile) {
        super(stile, new Item.Properties().group(ModSetup.itemGroup));
        this.setRegistryName("turn_stile");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        World world = context.getWorld();
        PlayerEntity entity = context.getPlayer();
        ActionResultType actionResultType = super.onItemUse(context);
        Direction direction = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,direction);
        BlockPos MainPos = (dhs == DoorHingeSide.RIGHT) ? pos.offset(direction.rotateY()): pos.offset(direction.rotateYCCW());
        TurnStileKeyItem key = (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem();
        if (actionResultType == ActionResultType.SUCCESS){
            System.out.println("block successfully put !!");
            ItemStack newStack = new ItemStack(key);
            key.setTSPosition(newStack,world,MainPos.up());
            System.out.println("itemstack of new key item : "+newStack);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,newStack);
            return super.onItemUse(context);
        }
        System.out.println("block non fabriqué");
        return ActionResultType.FAIL;
    }


}
