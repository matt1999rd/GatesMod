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
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
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
        TurnStileKeyItem key = (TurnStileKeyItem) ModItem.TURN_STILE_KEY.asItem();
        if (checkFeasibility(pos.up(),entity,world)){
            System.out.println("block successfully put !!");
            ItemStack newStack = new ItemStack(key);
            key.setTSPosition(newStack,world,pos.up());
            System.out.println("itemstack of new key item : "+newStack);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,newStack);
            return super.onItemUse(context);
        }
        System.out.println("block non fabriqué");
        return ActionResultType.FAIL;
    }

    private boolean checkFeasibility(BlockPos pos, PlayerEntity entity, World world) {
        Direction facing = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,facing);
        List<BlockPos> posList = new ArrayList<>();
        //block main
        posList.add(pos);
        //block right
        posList.add(pos.offset(facing.rotateYCCW()));
        //block left
        posList.add(pos.offset(facing.rotateY()));

        for (BlockPos pos_in : posList){
            //return false if the position of this future block is occupied by another solid block
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("la blockPos qui fait foirer :"+pos_in);
                System.out.println("Block qui bloque :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
            //return false if the position of this future block is above a air or bush or leaves block
            Block underBlock = world.getBlockState(pos_in.down()).getBlock();
            if (underBlock instanceof AirBlock || underBlock instanceof BushBlock || underBlock instanceof LeavesBlock){
                System.out.println("la blockPos qui fait foirer :"+pos_in.down());
                System.out.println("Block qui ne stabilise pas :"+underBlock);
                return false;
            }
        }
        return true;

    }
}
