package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.GarageDoor;
import fr.mattmouss.gates.setup.ModSetup;

import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;

import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;


public class GarageDoorItem extends BlockItem {
    public GarageDoorItem(GarageDoor garageDoor) {
        super(garageDoor,new Item.Properties().group(ModSetup.itemGroup));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (allBlockReplacedisAir(new BlockItemUseContext(context))){
            return super.onItemUse(context);
        }
        System.out.println("block non fabriqué");
        return ActionResultType.FAIL;
    }

    // todo : need to retest this function because bugs occurs when vincent used it
    // the function is not working  because we need to know where the first block will be created it depends on where the player clicked
    // need to check the onItemUse function of blockItem to know where it put the first value
    private boolean allBlockReplacedisAir(BlockItemUseContext context){
        World world = context.getWorld();
        PlayerEntity entity=context.getPlayer();
        BlockPos pos_base =context.getPos();
        Direction facing = Functions.getDirectionFromEntity(entity,pos_base);
        Direction dir_left_section=facing.rotateY();
        List<BlockPos> posList = new ArrayList<>();
        //block where we put the base block
        posList.add(pos_base);
        //back blocks
        posList.add(pos_base.up().offset(facing.getOpposite()));
        posList.add(pos_base.up()
                .offset(facing.getOpposite())
                .offset(dir_left_section));
        //left block
        posList.add(pos_base.offset(dir_left_section));
        posList.add(pos_base.offset(dir_left_section).up());

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
