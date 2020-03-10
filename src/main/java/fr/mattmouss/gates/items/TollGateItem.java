package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.setup.ModSetup;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
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
        TollKeyItem key = new TollKeyItem(pos);
        System.out.println("item defini : "+key);
        ItemStack stack = entity.getActiveItemStack();
        if (allBlockReplacedisAir(pos.up(),entity,world)){
            //TollKeyItem key = new TollKeyItem(pos);
            ItemStack newStack = new ItemStack(key);
            System.out.println("itemstack of new key item : "+newStack);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,ItemStack.EMPTY);
            entity.setItemStackToSlot(EquipmentSlotType.MAINHAND,newStack);

            return super.onItemUse(context);
        }
        System.out.println("block non fabriqué");
        return ActionResultType.FAIL;
    }

    private boolean allBlockReplacedisAir(BlockPos pos,PlayerEntity entity,World world){
        Direction facing = ModBlock.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = ModBlock.getHingeSideFromEntity(entity,pos,facing);
        Direction extDirection = ModBlock.getDirectionOfExtBlock(facing,dhs);
        List<BlockPos> posList = new ArrayList<>();
        //block de control unit
        posList.add(pos);
        //block main
        posList.add(pos.offset(facing.getOpposite()));
        //blocks de barrière fermé
        posList.add(pos.offset(facing.getOpposite()).offset(extDirection));
        posList.add(pos.offset(facing.getOpposite()).offset(extDirection,2));
        //block de barrière ouverte
        posList.add(pos.offset(facing.getOpposite()).up());
        posList.add(pos.offset(facing.getOpposite()).up(2));

        for (BlockPos pos_in : posList){
            //si le block n'est pas de l'air on retourne false
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("la blockPos qui fait foirer :"+pos_in);
                System.out.println("Block qui bloque :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
        }
        return true;
    }

}
