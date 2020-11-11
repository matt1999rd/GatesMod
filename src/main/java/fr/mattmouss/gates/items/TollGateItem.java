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
        TollKeyItem key = (TollKeyItem) ModItem.TOLL_GATE_KEY.asItem();
        System.out.println("item defini : "+key);
        if (checkFeasibility(new BlockItemUseContext(context))){
            ItemStack newStack = new ItemStack(key);
            key.setTGPosition(newStack,world,pos.up());
            System.out.println("itemstack of new key item : "+newStack);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,newStack);
            return super.onItemUse(context);
        }
        System.out.println("block non fabriqué");
        return ActionResultType.FAIL;
    }

    private boolean checkFeasibility(BlockItemUseContext context){
        BlockPos pos =context.getPos();
        PlayerEntity entity = context.getPlayer();
        World world = context.getWorld();
        Direction facing = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = Functions.getHingeSideFromEntity(entity,pos,facing);
        Direction extDirection = Functions.getDirectionOfExtBlock(facing,dhs);
        List<BlockPos> posList = new ArrayList<>();
        //block de control unit
        posList.add(pos);
        //block main
        posList.add(pos.offset(facing.getOpposite()));
        //blocks de barrière fermé
        posList.add(pos.offset(facing.getOpposite()).offset(extDirection));
        posList.add(pos.offset(facing.getOpposite()).offset(extDirection,2));
        //block de barrière ouverte
        BlockPos ignoredPos = pos.offset(facing.getOpposite()).up();
        posList.add(pos.offset(facing.getOpposite()).up());

        for (BlockPos pos_in : posList){
            //return false if the position of this future block is occupied by another solid block
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("la blockPos qui fait foirer :"+pos_in);
                System.out.println("Block qui bloque :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
            //return false if the position of this future block is above a air or bush block
            Block underBlock = world.getBlockState(pos_in.down()).getBlock();
            if (underBlock instanceof AirBlock || underBlock instanceof BushBlock || underBlock instanceof LeavesBlock){
                System.out.println("la blockPos qui fait foirer :"+pos_in.down());
                System.out.println("Block qui ne stabilise pas :"+underBlock);
                if (!pos_in.equals(ignoredPos)){
                    return false;
                }
            }
        }
        return true;
    }


}
